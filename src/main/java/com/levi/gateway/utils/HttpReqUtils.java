package com.levi.gateway.utils;

import com.alibaba.fastjson2.JSONObject;
import com.levi.gateway.domin.HttpResp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpReqUtils {

    public static HttpResp getSync(String url, HttpClient.Version httpVersion) {
        HttpClient client = HttpClient.newBuilder().version(httpVersion).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return HttpResp.builder().body(response.body()).statusCode(response.statusCode()).headers( response.headers()).build();
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

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
            return HttpResp.builder().body(response.body()).statusCode(response.statusCode()).headers(headers).build();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
