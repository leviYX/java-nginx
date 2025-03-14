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

        Router router = Router.router(vertx);
        router.get("/hello").handler(routingContext -> {
            logger.info("get hello request proxy received");
            HttpServerResponse response = routingContext.response();
            HttpServerRequest request = routingContext.request();
            response.end("Hello World");
        });

        router.post("/hello")
                .handler(BodyHandler.create())
                .handler(routingContext -> {
                    JsonObject body = routingContext.getBodyAsJson();
                    String name = body.getString("name");
                    routingContext.response().end(String.format("Hello %s!", name)).onSuccess(response -> {
                        logger.info("返回成功");
                    });
        });


        server.requestHandler(router)
                .listen(8080,event -> {
                    if (event.succeeded()) {
                        System.out.println("Server started on port 8080");
                    }
                });
    }
}
