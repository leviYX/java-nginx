package com.levi.gateway.launcher;

import io.vertx.core.Launcher;

public class GatewayLauncher  extends Launcher {
    public static void main(String[] args) {
        new GatewayLauncher().dispatch(args);
    }
}
