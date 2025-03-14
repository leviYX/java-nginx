package com.levi.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
        vertx.deployVerticle(new ProxyVerticle());
        vertx.deployVerticle(ServerVerticle.class,
                new DeploymentOptions()
                        .setInstances(Runtime.getRuntime().availableProcessors())
                        .setWorker(true));
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        logger.info("Starting MainVerticle");
    }
}
