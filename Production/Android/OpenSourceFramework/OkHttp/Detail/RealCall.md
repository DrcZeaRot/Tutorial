### RealCall

构造方法：
```
private RealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
    this.client = client;
    this.originalRequest = originalRequest;
    this.forWebSocket = forWebSocket;
    this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client, forWebSocket);
}
```

#### 同步执行

```
  @Override public Response execute() throws IOException {
    //Step1
    synchronized (this) {
      if (executed) throw new IllegalStateException("Already Executed");
      executed = true;
    }
    //Step2
    captureCallStackTrace();
    //Step3
    eventListener.callStart(this);
    try {
      //Step4
      client.dispatcher().executed(this);
      //Step5
      Response result = getResponseWithInterceptorChain();
      if (result == null) throw new IOException("Canceled");
      return result;
    } catch (IOException e) {
      eventListener.callFailed(this, e);
      throw e;
    } finally {
      client.dispatcher().finished(this);
    }
  }
```
流程如下：
1. 同步锁保证每个Call只能被Execute一次
2. 捕获当前请求的StackTrace，详见[StackTrace]()
3. EventListener回调(3.10版本依旧是非稳定的API、默认是空实现)，并不关键
4. Dispatcher::executed(final类，在OkHttpClient.Builder构造中创建，可以进行定制)
    * 将当前Call保存在Dispatcher的成员队列中，只是用于判断同步请求的并发量
    * 在finally中通过finished再从队列中移除
5. [getResponseWithInterceptorChain](../Interceptor/Interceptor.md)是通过拦截器，进行请求的具体过程，见下方详解
    * Response的创建、处理都在拦截器的proceed过程中完成
6. catch block进行EventListener的回调通知。

#### 异步执行
1. RealCall::enqueue
    ```
      @Override public void enqueue(Callback responseCallback) {
        synchronized (this) {
          if (executed) throw new IllegalStateException("Already Executed");
          executed = true;
        }
        captureCallStackTrace();
        eventListener.callStart(this);
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
      }
    ```
    * 在Dispatcher::enqueue之前的流程与同步执行没什么区别
    * AsyncCall是NamedRunnable(Runnable)的子类
        * NamedRunnable::run方法会执行抽象方法execute。
    * CallBack会在AsyncCall中回调
2. Dispatcher::enqueue
    ```
    synchronized void enqueue(AsyncCall call) {
        if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call) < maxRequestsPerHost) {
            runningAsyncCalls.add(call);
            executorService().execute(call);
        } else {
            readyAsyncCalls.add(call);
        }
    }
    ```
    1. 如果当前请求队列没有满(maxRequests默认60个)，并且每个Host能接受的请求数(maxRequestsPerHost默认5个)也符合要求
        * 则添加Call到队列，并通过executorService()执行AsyncCall
    2. 否则，只将AsyncCall添加到队列中
    3. promoteCalls方法中会对之前未直接符合执行条件的Call进行执行
        * 此方法在如下3中情况下会被执行：
        1. 更改了最大请求数maxRequests
        2. 更改了单Host的最多接受请求数maxRequestsPerHost
        3. 上一个异步请求AsyncCall的execute的finally block中调用finished(AsyncCall call)方法，会跳转本方法
3. Dispatcher::promoteCalls
    ```
      private void promoteCalls() {
        if (runningAsyncCalls.size() >= maxRequests) return; // Already running max capacity.
        if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.

        for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
          AsyncCall call = i.next();

          if (runningCallsForHost(call) < maxRequestsPerHost) {
            i.remove();
            runningAsyncCalls.add(call);
            executorService().execute(call);
          }

          if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
        }
      }
    ```
    1. 当前执行(running)的异步请求已经达到最大请求数：return
    2. 并没有处于ready状态的异步请求：return
    3. 遍历ready状态的异步请求
        1.  同host请求数没有达到最大的话
            1. 从ready容器中移除这个call
            2. 添加call到running的容器中
            3. Executor::execute，线程池执行请求，执行AsyncCall::run => AsyncCall::execute
        2. 如果遍历中，running的请求再次达到最大：return
4. NamedRunnable::run
    ```
      @Override public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
          execute();
        } finally {
          Thread.currentThread().setName(oldName);
        }
      }
    ```
    * 先将当前Thread的Name改为当前NamedRunnable的name
    * execute方法执行之后，再改回原来的oldName
5. AsyncCall
    ```
    AsyncCall(Callback responseCallback) {
      super("OkHttp %s", redactedUrl());//Thread的新Name来自这里
      this.responseCallback = responseCallback;
    }

    @Override protected void execute() {
      boolean signalledCallback = false;
      try {
        Response response = getResponseWithInterceptorChain();
        if (retryAndFollowUpInterceptor.isCanceled()) {
          signalledCallback = true;
          responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
        } else {
          signalledCallback = true;
          responseCallback.onResponse(RealCall.this, response);
        }
      } catch (IOException e) {
        if (signalledCallback) {
          // Do not signal the callback twice!
          Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
        } else {
          eventListener.callFailed(RealCall.this, e);
          responseCallback.onFailure(RealCall.this, e);
        }
      } finally {
        client.dispatcher().finished(this);
      }
    }
    ```
    1. 通过[getResponseWithInterceptorChain](../Interceptor/Interceptor.md)拦截器链的处理，获取最终Response。如果抛出异常，直接走catch
    2. 判断retryAndFollowUpInterceptor::isCanceled
        1. 当前Call如果已经被cancel，就走onFailure回调
        2. 如果没有被cancel，就走onResponse回调
    3. catch block进行回调
    4. finally block通知Dispatcher::finished


