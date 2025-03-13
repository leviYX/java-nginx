package com.levi.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
        vertx.deployVerticle(new ServerVerticle());
        vertx.deployVerticle(new ProxyVerticle());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        System.out.println("MainVerticle started");
    }
}
