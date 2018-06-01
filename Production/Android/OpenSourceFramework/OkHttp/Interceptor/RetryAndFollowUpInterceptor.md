### RetryAndFollowUpInterceptor

* 用户定义的普通拦截器之后的第一个拦截器
* 核心功能：
    1. 连接失败重试(Retry)；
        ```
        发生RouteException或IOException后，根据一定策略判断是否可以恢复
        如果可恢复则重新建立StreamAllocation开始新一轮请求
        ```
    2. 继续发起请求(Follow up)，
        * 主要类型：
            1. 3XX重定向
            2. 401、407未授权，调用Authenticator进行授权后发起新的请求
            3. 408客户端请求超时：Request请求体没有被UnrepeatableRequestBody标记，则继续发起新的请求
        * 次数受限：private static final int MAX_FOLLOW_UPS = 20;

[流程示意图](../img/RetryAndFollowUpInterceptor.png)

#### 流程简析

分段解析intercept方法：
1. 前戏阶段：
    * 从chain中获取request、call eventListener
    * 创建StreamAllocation实例，之后传递给proceed方法(需要一个Address实例)
2. while(true)，尝试retry&followUp：
    1. 尝试realChain.proceed(request, streamAllocation, null, null)，声明releaseConnection=true
        ```
          Response response;
          boolean releaseConnection = true;
          try {
            response = realChain.proceed(request, streamAllocation, null, null);
            releaseConnection = false;
          } catch (RouteException e) {
            // The attempt to connect via a route failed. The request will not have been sent.
            if (!recover(e.getLastConnectException(), streamAllocation, false, request)) {
              throw e.getLastConnectException();
            }
            releaseConnection = false;
            continue;
          } catch (IOException e) {
            // An attempt to communicate with a server failed. The request may have been sent.
            boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
            if (!recover(e, streamAllocation, requestSendStarted, request)) throw e;
            releaseConnection = false;
            continue;
          } finally {
            // We're throwing an unchecked exception. Release any resources.
            if (releaseConnection) {
              streamAllocation.streamFailed(null);
              streamAllocation.release();
            }
          }
        ```
        1. RouteException捕获：判断recover是否可行
            ```
            这个异常发生在 Request 请求还没有发出去前，就是打开 Socket 连接失败。
            这个异常是 OkHttp 自定义的异常，是一个包裹类，包裹住了建联失败中发生的各种 Exception
            主要发生 ConnectInterceptor 建立连接环节
            比如连接超时抛出的 SocketTimeoutException，包裹在 RouteException 中
            ```
        2. IOException捕获：
            ```
            这个异常发生在 Request 请求发出并且读取 Response 响应的过程中，TCP 已经连接，或者 TLS 已经成功握手后，连接资源准备完毕
            主要发生在 CallServerInterceptor 中，通过建立好的通道，发送请求并且读取响应的环节
            比如读取超时抛出的 SocketTimeoutException
            ```
        3. 异常处理，通过 recover 方法来判断是否是不可以重试的：
            1. 不可重试：继续抛出异常、finally
                ```
                OkHttp 有个黑名单机制，用来记录发起失败的 Route，从而在连接发起前将之前失败的 Route 延迟到最后再使用
                streamFailed 方法可以将这个出问题的 route 记录下来，放到黑名单（RouteDatabase）
                所以下一次发起新请求的时候，上次失败的 Route 会延迟到最后再使用，提高了响应成功率
                ```
            2. 可重试：releaseConnection = false、finally、continue
                ```
                继续使用 StreamAllocation 开始新的 proceed
                每一次重试，都会调用 RouteSelector 的 next 方法获取新的 Route，当没有可用的 Route 后就不会再重试了
                ```
        4. finally：
            * 上述两种异常情况下，如果判断可以recover，则releaseConnection=false
            * 如果出现了除了上述两种Exception的异常，releaseConnection依旧为true
            * if(releaseConnection)，对StreamAllocation进行release
    2. 重试判断：
        ```
          private boolean recover(IOException e, StreamAllocation streamAllocation,
              boolean requestSendStarted, Request userRequest) {
            streamAllocation.streamFailed(e);

            // The application layer has forbidden retries.
            if (!client.retryOnConnectionFailure()) return false;

            // We can't send the request body again.
            if (requestSendStarted && userRequest.body() instanceof UnrepeatableRequestBody) return false;

            // This exception is fatal.
            if (!isRecoverable(e, requestSendStarted)) return false;

            // No more routes to attempt.
            if (!streamAllocation.hasMoreRoutes()) return false;

            // For failure recovery, use the same route selector with a new connection.
            return true;
          }
        ```
        * StreamAllocation::streamFailed，记录Route黑名单并释放资源
        1. 判断Client是否允许重试，不允许返回false(默认是允许的，构建builder时定义)
        2. 不是被RouteException装饰的异常、且Body是UnrepeatableRequestBody，返回false
        3. 通过isRecoverable方法，无法过滤掉的，返回false
        4. 已经没有其他的路由可以使用
    3. 重新请求
        ```
        Request followUp = followUpRequest(response, streamAllocation.route());
        ```
        ```
        如果连接成功，也获得了 Response 响应，但是不一定是 200 OK，还有一些其他情况。
        比如 3xx 重定向，401 未授权等，这些响应码是允许我们再次发起请求的。
        比如重定向，获取目标地址后，再次发起请求。
        又比如 401 未授权，可以在 Request 中新增头部 “Authorization” 授权信息再次发起请求等
        ```
        1. 未授权
            1. 407代理未授权：在请求中添加 “Proxy-Authorization”
            2. 401未授权：在请求中添加 “Authorization”
        2. 重定向
            1. 307 和 308：如果不是 GET 或者 HEAD 请求不进行重定向
            2. 300，301，302，303：均允许重定向
            3. 具体的流程如下：
               1. 从响应中获取 “Location”
               1. 跳转到不支持协议不能重定向
               1. HTTPS 和 HTTP 之间的重定向，需要根据配置 followSslRedirects 来判断
               1. 去掉请求体，并响应地去掉 “Transfer-Encoding”，“Content-Length”，“Content-Type” 等头部信息
               1. 如果不是同一个连接（比如 host 不同），去掉授权头部 “Authorization”
        3. 超时：只处理一种情况
            1. 408 客户端超时
                ```
                部分服务器会因为客户端请求时间太长而返回 408，
                此时如果请求体没有实现标记接口 UnrepeatableRequestBody， OkHttp 会再把之前的请求没有修改重新发出
                ```



#### 参考

[浅析 OkHttp 拦截器之 RetryAndFollowUpInterceptor ](https://www.jianshu.com/p/08173e58670d)