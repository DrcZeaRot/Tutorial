### ConnectInterceptor

类注释：
```
打开一个与目标Server的Connection，并处理下一个拦截器
```

类实现：
```
/** Opens a connection to the target server and proceeds to the next interceptor. */
public final class ConnectInterceptor implements Interceptor {
  public final OkHttpClient client;

  public ConnectInterceptor(OkHttpClient client) {
    this.client = client;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    RealInterceptorChain realChain = (RealInterceptorChain) chain;
    Request request = realChain.request();
    StreamAllocation streamAllocation = realChain.streamAllocation();

    // We need the network to satisfy this request. Possibly for validating a conditional GET.
    boolean doExtensiveHealthChecks = !request.method().equals("GET");
    HttpCodec httpCodec = streamAllocation.newStream(client, chain, doExtensiveHealthChecks);
    RealConnection connection = streamAllocation.connection();

    return realChain.proceed(request, streamAllocation, httpCodec, connection);
  }
}
```
1. [StreamAllocation](../AbstractType/StreamAllocation.md)在RetryAndFollowUpInterceptor中创建
2. 通过StreamAllocation的newStream方法：创建Connection并根据协议创建不同的HttpCodec
3. 获取之前创建的RealConnection
4. 继续处理接下来的拦截器