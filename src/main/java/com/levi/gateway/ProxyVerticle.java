package com.levi.gateway;

import com.levi.gateway.domin.Frontend;
import com.levi.gateway.domin.UpStream;
import com.levi.gateway.enums.HttpStatusCode;
import com.levi.gateway.handler.WebSocketHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pipe;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProxyVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ProxyVerticle.class);

    @Override
    public void start()  {

        // 创建HttpServer
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setTcpKeepAlive(true);
        HttpServer server = vertx.createHttpServer(httpServerOptions);
        Router router = Router.router(vertx);

        // 读取配置文件，获取代理端口和upStream列表
        String page404 = config().getString("404");
        var proxyPort = config().getInteger("port");
        var upStreamArray = config().getJsonArray("upStream");
        var frontendArray = config().getJsonArray("frontend");
        List<UpStream> upStreamList = new ArrayList<>();
        List<Frontend> frontendList = new ArrayList<>();
        upStreamArray.forEach(upStream -> {upStreamList.add(new UpStream((JsonObject) upStream,vertx));});
        frontendArray.forEach(frontendEntry -> {
            Frontend frontend = new Frontend((JsonObject) frontendEntry);
            frontendList.add(frontend);
            router.route(frontend.getPrefix()).handler(
                    StaticHandler.create()
                            .setAllowRootFileSystemAccess(true)
                            .setWebRoot(frontend.getDir())
                           // .setCachingEnabled(true)
                           // .setCacheEntryTimeout(65535)
                           // .setMaxCacheSize(65535)
            );
        });
        // 处理404错误
        router.errorHandler(HttpStatusCode.NOT_FOUND.getCode(), routingContext -> {
            for (Frontend frontend : frontendList) {
                if (routingContext.request().path().startsWith(frontend.getPrefix())) {
                    routingContext.reroute(frontend.getReroute());
                    return;
                }
            }
            routingContext.reroute(page404);
        });

        server.webSocketHandler(new WebSocketHandler(upStreamList));

        // 外部端口的请求走requestHandler处理器
        server.requestHandler(reqUpstream -> {
            // 处理静态文件
            for (Frontend frontend : frontendList) {
                if (reqUpstream.path().startsWith(frontend.getPrefix())) {
                    router.handle(reqUpstream);
                    return;
                }
            }
            // 处理代理的接口请求
            for (UpStream upStream : upStreamList) {
                String targetPath = reqUpstream.path().replace(upStream.getPrefix(), upStream.getPath());
                // 遍历所有的upStream，判断请求的路径是否匹配
                if (reqUpstream.path().startsWith(upStream.getPrefix())) {
                    String upgrade = reqUpstream.getHeader("Upgrade");
                    // 构建pipe，执行pause();
                    Pipe<Buffer> pipe = reqUpstream.pipe();
                    // 构建返回对象，返回客户端
                    HttpServerResponse response = reqUpstream.response();
                    HttpClient upStreamHttpClient = upStream.getHttpClient();

                    // 处理websocket请求
                    if (upgrade != null && upgrade.equalsIgnoreCase("websocket")) {
                        Future<ServerWebSocket> webSocket = reqUpstream.toWebSocket();
                        webSocket
                                .onSuccess(ws -> {
                                    upStreamHttpClient.webSocket(targetPath).onSuccess(wsClient -> {
                                        ws.frameHandler(ss -> {
                                            logger.info("11111231231231,{}", ss.textData());
                                            wsClient.writeFrame(ss);
                                        });
                                        wsClient.frameHandler(frame -> {
                                            logger.info("666666666666666,{}", frame.textData());
                                            ws.writeFrame(frame);
                                        });
                                    });
                                })
                                .onFailure(err -> {logger.error("websocket error {}",err);});
                        return;
                    }

                    // 处理普通请求
                    upStreamHttpClient.request(reqUpstream.method(),targetPath)
                            .onSuccess(targetRequest -> {
                                targetRequest.headers().setAll(reqUpstream.headers());
                                // 代理服务发去目标服务
                                pipe.to(targetRequest);
                                // 代理服务返回客户端
                                targetRequest.response().onSuccess(targetResponse -> {
                                    response.headers().setAll(targetResponse.headers());
                                    targetResponse.pipeTo(response);
                                });
                            })
                            .onFailure(Throwable::printStackTrace);
                    break;
                }
            }
        }).listen(proxyPort,event -> { // 监听外部端口
            if (event.succeeded()) {
                vertx.deployVerticle(new ServerVerticle());
                logger.info("proxy server started on port {}",proxyPort);
            }
        });
    }
}
