package com.levi.gateway.handler;

import com.levi.gateway.domin.Frontend;
import com.levi.gateway.domin.UpStream;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pipe;
import io.vertx.ext.web.Router;


import java.util.List;

/**
 *   外部端口的请求走requestHandler处理器,
 *   1. 处理静态文件
 *   2. 处理代理的接口请求
 *
 */
public class RequestHandler implements Handler<HttpServerRequest> {

    private List<UpStream> upStreamList;
    private List<Frontend> frontendList ;
    private Router router;

    public RequestHandler(List<UpStream> upStreamList, List<Frontend> frontendList,Router router) {
        this.upStreamList = upStreamList;
        this.frontendList = frontendList;
        this.router = router;
    }

    @Override
    public void handle(HttpServerRequest reqUpstream) {

        // 处理静态文件 todo 可以优化循环遍历，改为map 时间复杂度O(1)
        for (Frontend frontend : frontendList) {
            if (reqUpstream.path().startsWith(frontend.getPrefix())) {
                router.handle(reqUpstream);
                return;
            }
        }

        // 处理代理的接口请求
        for (UpStream upStream : upStreamList) {
            String targetPath = reqUpstream.path().replace(upStream.getPrefix(), upStream.getPath());
            // 遍历所有的upStream，判断请求的路径是否匹配
            if (reqUpstream.path().startsWith(upStream.getPrefix())) {
                // 用来判断请求头中是不是包含Upgrade：websocket ，如果包含，说明是websocket协议，http协议需要升级到websocket协议
                // String upgrade = reqUpstream.getHeader("Upgrade");
                // if (upgrade != null && upgrade.equalsIgnoreCase("websocket")) {
                //     // 处理websocket代理请求
                //     WebSocketHandler webSocketHandler = new WebSocketHandler(upStreamList);
                // 构建pipe，执行pause();
                Pipe<Buffer> pipe = reqUpstream.pipe();
                // 构建返回对象，返回客户端
                HttpServerResponse response = reqUpstream.response();
                HttpClient upStreamHttpClient = upStream.getHttpClient();

                // 处理普通请求
                upStreamHttpClient.request(reqUpstream.method(),targetPath)
                        .onSuccess(targetRequest -> {
                            targetRequest.headers().setAll(reqUpstream.headers());
                            // 代理服务发去目标服务
                            pipe.to(targetRequest);
                            // 代理服务返回客户端
                            targetRequest.response().onSuccess(targetResponse -> {
                                response.headers().setAll(targetResponse.headers());
                                targetResponse.pipeTo(response);
                            });
                        })
                        .onFailure(Throwable::printStackTrace);
                break;
            }
        }
    }
}
