package com.levi.gateway;

import com.levi.gateway.constant.NetConstant;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.*;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(ProxyVerticle.class);

    @Override
    public void start()  {
        HttpServer server = vertx.createHttpServer();

        // 构建发送去目标服务的客户端
        HttpClientOptions httpClientOptions = new HttpClientOptions()
                .setDefaultHost("127.0.0.1")
                .setDefaultPort(8080)
//                .setDefaultPort(443)
//               .setSsl(true)
//               .setTrustAll(true)
                .setKeepAlive(true);
        HttpClient httpClient = vertx.createHttpClient(httpClientOptions);

        // 8081端口的请求走requestHandler处理器
        server.requestHandler(sourceRequest -> {
            // 构建返回对象，返回客户端
            HttpServerResponse response = sourceRequest.response();
            HttpMethod method = sourceRequest.method();
            String uri = sourceRequest.uri();
            MultiMap headers = sourceRequest.headers();

            if (method == HttpMethod.GET) {
                // 拿到代理请求，转发目标服务，注意这里request是构建请求对象，真实发送请求在下面的send中
                httpClient.request(method, uri, createReq -> {
                    if (createReq.succeeded()) {
                        HttpClientRequest request = createReq.result();
                        headers.forEach(header -> {
                            if ("Content-Type".equals(header.getKey())) {
                                request.putHeader(header.getKey(), header.getValue());
                            }
                        });
                        // 代理发送数据去目标服务
                        request.send()
                                .onSuccess(targetReps ->{
                                    // 拿到目标服务的响应，返回给客户端
                                    targetReps.bodyHandler(body -> {
                                        response.setStatusCode(targetReps.statusCode());
                                        response.end(body.toString());
                                    });
                                })
                                .onFailure(err ->{
                                    err.printStackTrace();
                                    response.setStatusCode(500).end(err.getMessage());
                                });
                    }
                });
            }
            if (method == HttpMethod.POST) {
                sourceRequest.body().onSuccess(resp -> {
                    httpClient.request(method, uri, createReq -> {
                        if (createReq.succeeded()) {
                            HttpClientRequest request = createReq.result();
                            request.send(resp).onSuccess(postSendReps -> {
                                postSendReps.bodyHandler(body -> {
                                    response.setStatusCode(postSendReps.statusCode());
                                    response.end(body.toString());
                                });
                            });
                        }
                    });
                });
            }
        }).listen(NetConstant.PROXY_PORT,event -> { // 监听外部端口
            if (event.succeeded()) {
                logger.info("proxy server started on port {}",NetConstant.PROXY_PORT);
            }
        });
    }
}
