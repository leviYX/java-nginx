package com.levi.gateway.domin;

import io.vertx.core.json.JsonObject;



public class Frontend {

    private String prefix;
    private String dir;
    private String reroute;

    public Frontend(JsonObject jsonObject) {
        this.prefix = jsonObject.getString("prefix");
        this.dir = jsonObject.getString("dir");
        this.reroute = jsonObject.getString("reroute");
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getReroute() {
        return reroute;
    }

    public void setReroute(String reroute) {
        this.reroute = reroute;
    }
}
