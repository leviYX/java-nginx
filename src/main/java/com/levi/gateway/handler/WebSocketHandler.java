package com.levi.gateway.handler;

import com.levi.gateway.domin.UpStream;
import io.vertx.core.Handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.streams.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class WebSocketHandler implements Handler<ServerWebSocket> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    private List<UpStream> upStreamList;

    public WebSocketHandler(List<UpStream> upStreamList) {
        this.upStreamList = upStreamList;
    }

    @Override
    public void handle(ServerWebSocket webSocket) {
        String wsPath = webSocket.path();
        String textHandlerID = webSocket.textHandlerID();
        // 处理代理的接口请求
        for (UpStream upStream : upStreamList) {
            String targetPath = wsPath.replaceFirst(upStream.getPrefix(), upStream.getPath());
            // 遍历所有的upStream，判断请求的路径是否匹配
            if (wsPath.startsWith(upStream.getPrefix())) {
                HttpClient httpClient = upStream.getHttpClient();
                webSocket.frameHandler(upFrame -> {
                    logger.info("***********接收到客户端的消息为{}", upFrame.textData());
                    httpClient.request(HttpMethod.GET,targetPath)
                            .onSuccess(targetRequest -> {
                                // 代理服务发去目标服务
                                targetRequest.send(Buffer.buffer(upFrame.textData()))
                                        .onSuccess(targetResponse -> {
                                            targetResponse.bodyHandler(buffer -> {
                                                logger.info("***********接收到服务端的消息为{}", buffer.toString());
                                                webSocket.writeFinalTextFrame(buffer.toString());
                                            });
                                        });
                            })
                            .onFailure(Throwable::printStackTrace);
                });
            }
        }
    }
}
