package com.levi.gateway;

import com.levi.gateway.constant.NetConstant;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.streams.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(ProxyVerticle.class);

    @Override
    public void start()  {
        HttpServer server = vertx.createHttpServer();
        // 构建发送去目标服务的客户端
        HttpClientOptions httpClientOptions = new HttpClientOptions().setDefaultHost(NetConstant.LOCALHOST).setDefaultPort(NetConstant.HTTP_PORT);
        HttpClient httpClient = vertx.createHttpClient(httpClientOptions);

        // 8081端口的请求走requestHandler处理器
        server.requestHandler(sourceRequest -> {
            // 构建pipe，执行pause();
            Pipe<Buffer> pipe = sourceRequest.pipe();
            // 构建返回对象，返回客户端
            HttpServerResponse response = sourceRequest.response();
           httpClient.request(sourceRequest.method(), sourceRequest.uri())
                   .onSuccess(targetRequest -> {
                       sourceRequest.headers().forEach(header -> {
                           if ("Content-Type".equals(header.getKey())) {
                               targetRequest.putHeader(header.getKey(), header.getValue());
                           }
                       });
                       // 代理服务发去目标服务
                       pipe.to(targetRequest);
                       // 代理服务返回客户端
                       targetRequest.response().onSuccess(targetResponse -> {
                           targetResponse.headers().forEach(header -> response.putHeader(header.getKey(), header.getValue()));
                           targetResponse.pipeTo(response);
                       });
                   })
                   .onFailure(Throwable::printStackTrace);
        }).listen(NetConstant.PROXY_PORT,event -> { // 监听外部端口
            if (event.succeeded()) {
                logger.info("proxy server started on port {}",NetConstant.PROXY_PORT);
            }
        });
    }
}
