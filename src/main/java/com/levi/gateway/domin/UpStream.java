package com.levi.gateway.domin;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.MalformedURLException;
import java.net.URL;

@Getter
@Setter
@ToString
public class UpStream {

    private String path;
    private String url;
    private String prefix;
    private HttpClient httpClient;

    public UpStream(JsonObject jsonObject, Vertx vertx) {

        this.url = jsonObject.getString("url");
        this.prefix = jsonObject.getString("prefix");

        try {
            URL uri = new URL(this.url);
            this.path = uri.getPath();
            // 构建发送去目标服务的客户端
            HttpClientOptions httpClientOptions = new HttpClientOptions()
                    .setDefaultHost(uri.getHost())
                    .setDefaultPort(uri.getPort());
            this.httpClient = vertx.createHttpClient(httpClientOptions);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
