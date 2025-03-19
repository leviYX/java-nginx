package com.levi.gateway;

import com.levi.gateway.domin.Frontend;
import com.levi.gateway.domin.UpStream;
import com.levi.gateway.enums.HttpStatusCode;
import com.levi.gateway.handler.RequestHandler;
import com.levi.gateway.handler.WebSocketHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ProxyVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ProxyVerticle.class);

    private static final String FAVICON_ICO = "/favicon.ico";

    @Override
    public void start()  {

        // 创建HttpServer
        var httpServerOptions = new HttpServerOptions().setTcpKeepAlive(true);
        HttpServer server = vertx.createHttpServer(httpServerOptions);
        Router router = Router.router(vertx);

        // 读取配置文件，获取代理端口和上游信息
        var page404 = config().getString("404");
        var proxyPort = config().getInteger("port");
        var upStreamArray = config().getJsonArray("upStream");
        var frontendArray = config().getJsonArray("frontend");
        var upStreamList = new ArrayList<UpStream>();
        var frontendList = new ArrayList<Frontend>();
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

        // 处理websocket代理请求
        server.webSocketHandler(new WebSocketHandler(upStreamList));

        // 处理普通请求包括静态文件
        server.requestHandler(new RequestHandler(upStreamList,frontendList,router));

        // 处理favicon.ico请求
        router.route(FAVICON_ICO).handler(routingContext -> {
            routingContext.response().end();
        });

        // 处理502错误
        router.errorHandler(HttpStatusCode.BAD_GATEWAY.getCode(), routingContext -> {
            routingContext.response().setStatusCode(HttpStatusCode.BAD_GATEWAY.getCode()).end();
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

        // 监听外部端口
        server.listen(proxyPort,listenEvent -> {
            if (listenEvent.succeeded()) {
                vertx.deployVerticle(new ServerVerticle());
                logger.info("proxy server started on port {}",proxyPort);
            }
        });
    }
}
