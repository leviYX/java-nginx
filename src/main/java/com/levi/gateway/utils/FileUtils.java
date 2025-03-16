package com.levi.gateway.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {

    public static void main(String[] args) {
        JSONObject jsonObject = file2Json("/Users/levi/develop/project/netty/vertx/gateway/gateway/src/main/resources/config.json");
        System.out.println(jsonObject.toString());
    }
    public static JSONObject file2Json(String filePath) {
        try {
            File file = new File(filePath);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String jsonString = new String(fileContent);
            return JSON.parseObject(jsonString);
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
