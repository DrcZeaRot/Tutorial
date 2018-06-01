### Route

类注释：
```
连接使用的路由到抽象服务器。创建连接时，客户端有很多选择
1、HTTP proxy(http代理)：
    已经为客户端配置了一个专门的代理服务器，
    否则会通过net.ProxySelector proxy selector尝试多个代理
2、IP address(ip地址)：
    无论是通过直连还是通过代理，DNS服务器可能会尝试多个ip地址。
```

* OkHttp3中抽象出来的Route是描述网络数据包传输的路径
    * 最主要还是描述直接与其建立TCP连接的目标端点
* 字段表
    ```
      final Address address;
      final Proxy proxy;
      final InetSocketAddress inetSocketAddress;
    ```
* 简析：
    ```
    Route通过代理服务器的信息proxy、链接的目标地址Address，来描述路由即Route
    连接的目标地址inetSocketAddress根据代理类型的不同而有着不同的含义，
    这主要是通过不同代理协议的差异而造成的。
    ```
    1. 对于无需代理的情况
        ```
        连接的目标地址inetSocketAddress中包含HTTP服务器经过DNS域名解析的IP地址以及协议端口号
        ```
    2. 对于SOCKET代理：其中包含HTTP服务器的域名及协议端口号
    3. 对于HTTP代理：其中则包含代理服务器经过域名解析的IP地址及端口号
* requiresTunnel
    ```
      public boolean requiresTunnel() {
        return address.sslSocketFactory != null && proxy.type() == Proxy.Type.HTTP;
      }
    ```
    * 一个安全的SSL连接、并设置了HTTP代理，则会建立一个到目标HTTP服务器的隧道连接(CONNECT方法与tunnel隧道)。

#### 参考

[OkHttp源码解析(四):OKHttp中阶之拦截器及调用链](https://www.jianshu.com/p/e3b6f821acb8)