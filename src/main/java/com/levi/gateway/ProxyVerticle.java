package com.levi.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;

public class ProxyVerticle extends AbstractVerticle {

    @Override
    public void start()  {
        HttpServer server = vertx.createHttpServer();

        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultHost("127.0.0.1");
        httpClientOptions.setDefaultPort(8080);
        HttpClient httpClient = vertx.createHttpClient(httpClientOptions);

        server.requestHandler(sourceRequest -> {
            HttpServerResponse response = sourceRequest.response();
            HttpMethod method = sourceRequest.method();
            String uri = sourceRequest.uri();
            // 转发请求
            httpClient.request(method, uri, ar1 -> {
                if (ar1.succeeded()) {
                    HttpClientRequest request = ar1.result();
                    request.send(ar2 -> {
                        if (ar2.succeeded()) {
                            HttpClientResponse targetResponse = ar2.result();
                            targetResponse.bodyHandler(body -> {
                                response.setStatusCode(targetResponse.statusCode());
                                response.end(body.toString());
                            });
                        }
                    });
                }
            });
        }).listen(9090,event -> {
            if (event.succeeded()) {
                System.out.println("Server started on port 9090");
            }
        });
    }
}
