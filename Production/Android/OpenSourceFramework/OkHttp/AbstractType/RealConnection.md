### RealConnection

是Connection接口的实际实现类
```
public interface Connection {
    Route route(); //返回一个路由
    Socket socket();  //返回一个socket
    Handshake handshake();  //如果是一个https,则返回一个TLS握手协议
    Protocol protocol(); //返回一个协议类型 比如 http1.1 等或者自定义类型
}
```


#### 参考

[OKHttp源码解析(九):OKHTTP连接中三个"核心"RealConnection、ConnectionPool、StreamAllocation](https://www.jianshu.com/p/6166d28983a2)