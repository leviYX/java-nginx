# 环境要求
jdk21+

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
可以使用.http文件进行测试(recource目录下提供了.http测试文件)，也可以使用postman进行测试
但是代码中提供了测试工具类，可以直接使用
```java
com.levi.gateway.util.HttpClientUtil
```
## 3、压力测试
recource目录下提供了jmx文件，可以使用jmeter进行测试

# 四、监控
可以通过gclog进行监控,但是需要在启动时添加JVM参数

recource目录下提供了default.jfc文件，可以使用JFR+JMC进行监控,但是需要在启动时添加JVM参数
```shell
-Xmx2048m 
-Xms2048m
-XX:StartFlightRecording=disk=true,maxsize=5000m,maxage=2d,settings=./default.jfc -XX:FlightRecorderOptions=maxchunksize=128m,repository=./,stackdepth=256
-XX:+UseZGC
-XX:+ZGenerational
-XX:MetaspaceSize=640m 
-XX:MaxMetaspaceSize=640m
-Xlog:safepoint,classhisto*=trace,age*,gc*=info:file=日志路径/gc-%t.log:time,tid,tags:filecount=5,filesize=50m
```

# 五、websocket
实现类为
```java
com.levi.gateway.websocket.WebSocketHandler
```
参考[官方websocket实现案例][https://github.com/vert-x3/vertx-examples/blob/4.x/core-examples/src/main/java/io/vertx/example/core/http/websockets/Server.java]
以及官方文档：https://vertx-china.github.io/docs/vertx-core/java/#_writing_http_servers_and_clients
