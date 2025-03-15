# 一、启动方式
## 1、Launcher方式启动(推荐)
项目参数添加 指定配置文件路径 以及 启动类名
```shell
java -jar gateway.jar -conf /Users/levi/develop/project/netty/vertx/gateway/gateway/src/main/resources/config.json -cp com.levi.gateway.ProxyVerticle
```
## 2、vertx方式启动
```shell
vertx run gateway.jar -cp com.levi.gateway.ProxyVerticle
```

# 二、配置文件
以json方式配置，配置文件路径通过启动参数指定
其中port为网关监听端口，upStream为代理配置，prefix为代理前缀，url为代理目标地址
```json
{
  "port": 9090,
  "upStream":[
    {
      "prefix":"/a",
      "url": "http://127.0.0.1:8080"
    }
  ]
}
```

# 三、测试方式
## 1、单元测试
todo
## 2、http测试
可以使用.http文件进行测试，也可以使用postman进行测试
但是代码中提供了测试工具类，可以直接使用
```java
com.levi.gateway.util.HttpClientUtil
```
## 3、压力测试
recource目录下提供了jmx文件，可以使用jmeter进行测试