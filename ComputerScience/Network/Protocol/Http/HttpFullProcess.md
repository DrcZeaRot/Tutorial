### 一次完整的Http请求流程

* 简单描述：
    1. 域名解析
    2. TCP的3次握手
    3. 建立TCP连接后发起HTTP请求
    4. 收到响应解析ResponseBody
* Http连接实际上就是TCP连接，和一些使用TCP连接的规则
    1. 从 URL 中解析出服务器的主机名
    1. 查询这个主机名对应的IP地址
    1. 获取端口号
    1. 发送连接到对应的 主机 : 端口
    1. 发送GET等请求
    1. 收到服务端的响应报文
    1. 关闭连接

[Http请求流程示意图](../img/HttpRequestProcess.png)

[Http协议与IP、TCP、DNS的交互示意图](../img/HttpAndHisFriends.png)