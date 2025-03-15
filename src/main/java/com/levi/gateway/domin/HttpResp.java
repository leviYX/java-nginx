package com.levi.gateway.domin;

import lombok.Builder;
import lombok.Data;

import java.net.http.HttpHeaders;

@Data
@Builder
public class HttpResp{
    private int statusCode;
    private String body;
    private HttpHeaders headers;
}