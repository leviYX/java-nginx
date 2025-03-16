package com.levi.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;



public class WebSocketVerticle  extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WebSocketVerticle());
    }
    @Override
    public void start() {
        vertx.createHttpServer()
                .webSocketHandler(ws -> ws.handler(ws::writeBinaryMessage))
                .requestHandler(req -> {
                    if (req.uri().equals("/")) req.response().sendFile("/Users/levi/develop/project/netty/vertx/gateway/gateway/src/test/resources/webroot/index.html");
                })
                .listen(8080)
                .onSuccess(reps ->{
                    System.out.println("HTTP server started on port 8080");
                });
    }
}
