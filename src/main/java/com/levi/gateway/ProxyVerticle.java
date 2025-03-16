package com.levi.gateway;

import com.levi.gateway.domin.Frontend;
import com.levi.gateway.domin.UpStream;
import com.levi.gateway.enums.HttpStatusCode;
import io.netty.handler.codec.http.HttpStatusClass;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pipe;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        router.errorHandler(HttpStatusCode.NOT_FOUND.getCode(), routingContext -> {
            for (Frontend frontend : frontendList) {
                if (routingContext.request().path().startsWith(frontend.getPrefix())) {
                    routingContext.reroute(frontend.getReroute());
                    return;
                }
            }
            routingContext.reroute(page404);
        });
        // 外部端口的请求走requestHandler处理器
        server.requestHandler(reqUpstream -> {
            // 处理静态文件
            for (Frontend frontend : frontendList) {
                if (reqUpstream.path().startsWith(frontend.getPrefix())) {
                    router.handle(reqUpstream);
                    return;
                }
            }


            for (UpStream upStream : upStreamList) {
                // 遍历所有的upStream，判断请求的路径是否匹配
                if (reqUpstream.path().startsWith(upStream.getPrefix())) {
                    String targetPath = reqUpstream.path().replace(upStream.getPrefix(), upStream.getPath());
                    // 构建pipe，执行pause();
                    Pipe<Buffer> pipe = reqUpstream.pipe();
                    // 构建返回对象，返回客户端
                    HttpServerResponse response = reqUpstream.response();
                    upStream.getHttpClient().request(reqUpstream.method(),targetPath)
                            .onSuccess(targetRequest -> {
                                reqUpstream.headers().forEach(header -> {
                                    if (header.getKey().equalsIgnoreCase("content-length")) {
                                        targetRequest.putHeader(header.getKey(), header.getValue());
                                    }
                                    if (header.getKey().equalsIgnoreCase("transfer-encoding")) {
                                        response.putHeader(header.getKey(), header.getValue());
                                    }
                                });
                                // 代理服务发去目标服务
                                pipe.to(targetRequest);
                                // 代理服务返回客户端
                                targetRequest.response().onSuccess(targetResponse -> {
                                    targetResponse.headers().forEach(header -> {
                                        if (header.getKey().equalsIgnoreCase("content-length")) {
                                            response.putHeader(header.getKey(), header.getValue());
                                        }
                                        if (header.getKey().equalsIgnoreCase("transfer-encoding")) {
                                            response.putHeader(header.getKey(), header.getValue());
                                        }
                                    });
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
