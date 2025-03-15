package com.levi.gateway;

import com.levi.gateway.domin.UpStream;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ProxyVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ProxyVerticle.class);

    @Override
    public void start()  {

        // 读取配置文件
        var proxyPort = config().getInteger("port");
        var upStreamArray = config().getJsonArray("upStream");
        if (upStreamArray.isEmpty()) return;
        List<UpStream> upStreamList = new ArrayList<>();
        upStreamArray.forEach(upStream -> {upStreamList.add(new UpStream((JsonObject) upStream,vertx));});

        HttpServer server = vertx.createHttpServer();

        // 8081端口的请求走requestHandler处理器
        server.requestHandler(reqUpstream -> {
            for (UpStream upStream : upStreamList) {
                // 遍历所有的upStream，判断请求的路径是否匹配
                if (reqUpstream.path().startsWith(upStream.getPrefix())) {
                    String targetPath = reqUpstream.path().replace(upStream.getPrefix(), upStream.getPath());
                    // 构建pipe，执行pause();
                    Pipe<Buffer> pipe = reqUpstream.pipe();
                    // 构建返回对象，返回客户端
                    HttpServerResponse response = reqUpstream.response();
                    upStream.getHttpClient().request(reqUpstream.method(),targetPath)
                            .onSuccess(targetRequest -> {
                                reqUpstream.headers().forEach(header -> {
                                    if (header.getKey().equalsIgnoreCase("content-length")) {
                                        targetRequest.putHeader(header.getKey(), header.getValue());
                                    }
                                    if (header.getKey().equalsIgnoreCase("transfer-encoding")) {
                                        response.putHeader(header.getKey(), header.getValue());
                                    }
                                });
                                // 代理服务发去目标服务
                                pipe.to(targetRequest);
                                // 代理服务返回客户端
                                targetRequest.response().onSuccess(targetResponse -> {
                                    targetResponse.headers().forEach(header -> {
                                        if (header.getKey().equalsIgnoreCase("content-length")) {
                                            response.putHeader(header.getKey(), header.getValue());
                                        }
                                        if (header.getKey().equalsIgnoreCase("transfer-encoding")) {
                                            response.putHeader(header.getKey(), header.getValue());
                                        }
                                    });
                                    targetResponse.pipeTo(response);
                                });
                            })
                            .onFailure(Throwable::printStackTrace);
                    break;
                }
            }
        }).listen(proxyPort,event -> { // 监听外部端口
            if (event.succeeded()) {
                vertx.deployVerticle(new ServerVerticle());
                logger.info("proxy server started on port {}",proxyPort);
            }
        });
    }
}
