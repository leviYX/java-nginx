package com.levi.gateway.handler;

import com.levi.gateway.domin.UpStream;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.ServerWebSocket;

import java.util.List;

/**
 *  外部端口的请求走websocket处理器,
 */
public class WebSocketHandler implements Handler<ServerWebSocket> {

    private List<UpStream> upStreamList;

    public WebSocketHandler(List<UpStream> upStreamList) {
        this.upStreamList = upStreamList;
    }

    @Override
    public void handle(ServerWebSocket webSocket) {
        // ws请求的路径
        String wsPath = webSocket.path();
        // 处理代理的接口请求 todo 可以优化循环遍历，改为map 时间复杂度O(1)
        for (UpStream upStream : upStreamList) {
            String targetPath = wsPath.replaceFirst(upStream.getPrefix(), upStream.getPath());
            // 遍历所有的upStream，判断请求的路径是否匹配
            if (wsPath.startsWith(upStream.getPrefix())) {
                HttpClient httpClient = upStream.getHttpClient();
                webSocket.frameHandler(upFrame -> {
                    httpClient.request(HttpMethod.GET,targetPath)
                            .onSuccess(targetRequest -> {
                                // 代理服务发去目标服务
                                targetRequest.send(Buffer.buffer(upFrame.textData()))
                                        .onSuccess(targetResponse -> {
                                            targetResponse.bodyHandler(buffer -> {
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
