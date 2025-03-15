package com.levi.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ServerVerticle.class);

    @Override
    public void start()  {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx)
                .errorHandler(500, routingContext -> {
                    routingContext.response().setStatusCode(routingContext.statusCode()).end("Internal Server Error");
                } )
                .errorHandler(404, routingContext -> {
                    routingContext.response().setStatusCode(routingContext.statusCode()).end("404 Not Found");
                });

        router.get("/hello").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            HttpServerRequest request = routingContext.request();
            response.end("Hello World");
        }).failureHandler(error -> logger.error("hello get 返回失败 {}", error));

        router.post("/hello")
                .handler(BodyHandler.create())
                .handler(routingContext -> {
                    JsonObject body = routingContext.getBodyAsJson();
                    String name = body.getString("name");
                    routingContext.response().end(String.format("Hello %s!", name)).onSuccess(response -> logger.info("返回成功"));
                }).failureHandler(error -> logger.error("hello post 返回失败 {}", error));


        server.requestHandler(router)
                .listen(8080,event -> {
                    if (event.succeeded()) {
                        System.out.println("Server started on port 8080");
                    }
                });
    }
}
