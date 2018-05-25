### OkHttp.Call
```
OkHttp的Http请求封装，能进行的请求操作都定义在这里。
Call的实例化，采用工厂方法模式
```

接口定义：
```
public interface Call extends Cloneable {
    //获取当前Request
    Request request();
    //同步请求
    Response execute() throws IOException;
    //异步请求
    void enqueue(Callback responseCallback);
    //取消请求
    void cancel();
    //当前Call是否已经执行
    boolean isExecuted();
    //当前Call是否已经取消
    boolean isCanceled();
    //clone
    Call clone();
    //抽象工厂
    interface Factory {
        Call newCall(Request request);
    }
}
```

工厂实现：[Call.Factory&OkhttpClient](Call.Factory&OkhttpClient.md)

Call实现：[RealCall](RealCall.md)