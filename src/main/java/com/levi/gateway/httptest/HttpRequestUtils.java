package com.levi.gateway.httptest;

import com.alibaba.fastjson2.JSONObject;
import com.levi.gateway.utils.HttpReqUtils;
import java.net.http.HttpClient;
import java.util.Map;

public class HttpRequestUtils {

    public static void main(String[] args) {
        String url = "http://localhost:9090/a/hello";
        HttpReqUtils.postSync(url, new JSONObject(Map.of("name", "java")),HttpClient.Version.HTTP_2);
    }
}