### Call.Factory&OkHttpClient

OkHttpClient是一个Call.Factory的实现，同时也实现了WebSocket.Factory。
```
public class OkHttpClient implements Cloneable, Call.Factory, WebSocket.Factory{

    @Override public Call newCall(Request request) {
        return RealCall.newRealCall(this, request, false /* for web socket */);
    }

    @Override public WebSocket newWebSocket(Request request, WebSocketListener listener) {
        RealWebSocket webSocket = new RealWebSocket(request, listener, new Random(), pingInterval);
        webSocket.connect(this);
        return webSocket;
    }
}
```

```
OkHttpClient在行为上采用外观模式，整体功能是构建Call/WebSockt实例。
除了两个Factory用于创建实例的方法以外，其余都是获取当前成员的getter方法。
OkHttpClient的实例化采用Builder模式。
```
