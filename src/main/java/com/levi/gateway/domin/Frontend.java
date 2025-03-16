package com.levi.gateway.domin;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Frontend {

    private String prefix;
    private String dir;
    private String reroute;

    public Frontend(JsonObject jsonObject) {
        this.prefix = jsonObject.getString("prefix");
        this.dir = jsonObject.getString("dir");
        this.reroute = jsonObject.getString("reroute");
    }

}
