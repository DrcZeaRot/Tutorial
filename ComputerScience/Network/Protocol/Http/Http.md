### Http

* HTTP 是一种不保存状态，即无状态(stateless)协议
    1. HTTP 协议自身不对请求和响应之间的通信状态进行保存
    2. 也就是说在 HTTP 这个级别，协议对于发送过的请求或响应都不做持久化处理
    3. 这是为了更快地处理大量事务，确保协议的可伸缩性，而特意把 HTTP 协议设计成如此简单的
* 为了实现期望的保持状态功能
    1. 于是引入了Cookie技术。
    2. 有了Cookie再用HTTP协议通信，就可以管理状态了
* 处于TCP/IP协议的应用层

#### Http关键定义

1. [请求方法](HttpMethod.md)
2. [响应状态码](HttpResponseCode.md)
3. [请求/响应Header](HttpHeader.md)
4. [请求/响应报文](HttpMessage.md)
5. [一次完整的Http请求流程](HttpFullProcess.md)
6. [Cookie](HttpCookie.md)

#### 持久连接&管线化

持久连接：
1. Http协议的初期版本中，每进行一次Http通信就要连接/断开一次TCP连接。
    1. 当HTML资源包含更多的资源时，会触发多次TCP的连接/断开，无谓地增加了通信量
2. Http/1.1中，为了解决这个问题，提出了持久连接的方法
    1. Http/1.1中默认开启，请求头中通过Connection控制:keep-alive保持开启、close一次请求完成后关闭。
    2. 效果是：只要任意一端没有明确提出断开，则保持TCP连接状态

[持久连接示意图](../img/HttpKeepAlive.png)

管线化：
1. 持久连接使得多个请求以管线化(PipeLining)方式发送成为可能
2. 从前，多个请求需要串行发送：上一个响应之后才能发送下一个
3. 管线化技术允许：多个请求并行发送
[管线化示意图](../img/HttpPipeLining.png)

#### Http协议的缺点：
1. 窃听风险（eavesdropping）：通信使用明文，第三方可以获知通信内容。
1. 冒充风险（pretending）：不验证通信方的身份，第三方可以冒充他人身份参与通信。
1. 篡改风险（tampering）：无法证明报文的完整性，第三方可以修改通信内容。
* 此时需要HTTPS
