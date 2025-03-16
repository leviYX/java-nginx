package com.levi.gateway;


import com.alibaba.fastjson2.JSONObject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(VertxExtension.class)
public class VerticleTest {

    private static final Logger LOG = LoggerFactory.getLogger(VerticleTest.class);

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        //vertx.deployVerticle(new ServerVerticle(), testContext.succeedingThenComplete());

        JSONObject configObj = JSONObject.parseObject("{\n" +
                "  \"port\": 9090,\n" +
                "  \"upStream\":[\n" +
                "    {\n" +
                "      \"prefix\":\"/a\",\n" +
                "      \"url\": \"http://127.0.0.1:8080\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        JsonObject config = new JsonObject(configObj);
        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config);

        vertx.deployVerticle(new ProxyVerticle(), deploymentOptions,testContext.succeedingThenComplete());
    }

    @Test
    void testProxyApi(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .get(9090, "localhost", "/a/hello")
                .send()
                .onSuccess(response -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    testContext.completeNow();
                }).onFailure(testContext::failNow);
    }

    @Test
    void testServerApi(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .get(8080, "localhost", "/hello")
                .send()
                .onSuccess(response -> {
                    assertThat(response.statusCode()).isEqualTo(200);
                    testContext.completeNow();
                }).onFailure(testContext::failNow);
    }

    @Test
    void start_http_server(Vertx vertx,VertxTestContext testContext) throws Throwable {
        vertx.createHttpServer()
                .requestHandler(req -> req.response().end("123456"))
                .listen(16969,response ->{
                    assertThat(response.succeeded()).isTrue();
                    // vertx都是异步的，如果没有这个，测试会直接通过
                    // 因为测试方法执行完了，vertx还没有执行完，所以需要等待vertx执行完
                    // 所以需要使用VertxTestContext来等待vertx执行完
                    // 这样就可以保证测试方法执行完了，vertx才会执行完
                    testContext.completeNow();
                });
    }

}
