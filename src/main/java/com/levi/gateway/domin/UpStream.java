package com.levi.gateway.domin;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
