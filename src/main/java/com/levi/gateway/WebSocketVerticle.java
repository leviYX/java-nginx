package com.levi.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;

import java.util.Date;


public class WebSocketVerticle  extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WebSocketVerticle());
    }
    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(request -> {
            if (request.path().equals("/myapi")) {
                Future<ServerWebSocket> fut = request.toWebSocket();
                fut.onSuccess(ws -> {
                    ws.writeTextMessage("Hello World" + new Date().getTime());
                });
            } else {
                request.response().setStatusCode(400).end();
            }
        }).listen(8080).onSuccess(reps ->{System.out.println("HTTP server started on port 8080");});
    }
}
