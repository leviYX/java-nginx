package com.levi.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

public class ServerVerticle extends AbstractVerticle {

    @Override
    public void start()  {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route("/hello").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            HttpServerRequest request = routingContext.request();
            response.end("Hello World");
        });

        server.requestHandler(router)
                .listen(8080,event -> {
                    if (event.succeeded()) {
                        System.out.println("Server started on port 8080");
                    }
                });
    }
}
