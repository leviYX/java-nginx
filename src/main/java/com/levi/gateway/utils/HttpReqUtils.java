package com.levi.gateway.utils;

import com.alibaba.fastjson2.JSONObject;
import com.levi.gateway.domin.HttpResp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpReqUtils {

    private static final String TEST_HTTP_URL = "http://localhost:9090/a/hello";

    public static void main(String[] args) {
        HttpReqUtils.postSync(TEST_HTTP_URL, new JSONObject(Map.of("name", "java")),HttpClient.Version.HTTP_2);
    }

    /**
     * 同步发送GET请求
     *
     * @param url 请求地址
     * @param httpVersion HTTP版本
     * @return HttpResp 响应对象
     */
    public static HttpResp getSync(String url, HttpClient.Version httpVersion) {
        HttpClient client = HttpClient.newBuilder().version(httpVersion).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var HttpResp = new HttpResp();
            HttpResp.setBody(response.body());
            HttpResp.setStatusCode(response.statusCode());
            HttpResp.setHeaders(response.headers());
            return HttpResp;
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 异步发送GET请求
     *
     * @param url
     * @param httpVersion
     * @return CompletableFuture<HttpResponse<String>> 异步future对象
     */
    public static CompletableFuture<HttpResponse<String>> getASync(String url, HttpClient.Version httpVersion) {
        HttpClient client = HttpClient.newBuilder().version(httpVersion).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * 同步发送POST请求
     *
     * @param url 请求地址
     * @param body 请求体
     * @param httpVersion HTTP版本
     * @return HttpResp 响应对象
     */
    public static HttpResp postSync(String url, JSONObject body, HttpClient.Version httpVersion) {
        HttpClient client = HttpClient.newBuilder().version(httpVersion).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            HttpHeaders headers = response.headers();
            HttpResp resp = new HttpResp();
            resp.setBody(response.body());
            resp.setHeaders(headers);
            resp.setStatusCode(response.statusCode());
            return resp;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 异步发送POST请求
     *
     * @param url 请求地址
     * @param body 请求体
     * @param httpVersion HTTP版本
     * @return CompletableFuture<HttpResponse<String>> 异步future对象
     */
    public static CompletableFuture<HttpResponse<String>>  postASync(String url, JSONObject body, HttpClient.Version httpVersion) {
        HttpClient client = HttpClient.newBuilder().version(httpVersion).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
