### HttpCodec

接口注释：
```
对HTTP请求进行编码、对HTTP响应进行解码。
```

主要功能：将网络请求的行为抽象为接口
1. 第一步，写入请求头
1. 第二步，写入请求头
1. 第三步，读取响应头
1. 第四步，读取响应体

接口定义：
```
/** Encodes HTTP requests and decodes HTTP responses. */
public interface HttpCodec {
  /**
   * The timeout to use while discarding a stream of input data. Since this is used for connection
   * reuse, this timeout should be significantly less than the time it takes to establish a new
   * connection.
   */
  int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

  /** Returns an output stream where the request body can be streamed. */
  Sink createRequestBody(Request request, long contentLength);

  /** This should update the HTTP engine's sentRequestMillis field. */
  void writeRequestHeaders(Request request) throws IOException;

  /** Flush the request to the underlying socket. */
  void flushRequest() throws IOException;

  /** Flush the request to the underlying socket and signal no more bytes will be transmitted. */
  void finishRequest() throws IOException;

  /**
   * Parses bytes of a response header from an HTTP transport.
   *
   * @param expectContinue true to return null if this is an intermediate response with a "100"
   *     response code. Otherwise this method never returns null.
   */
  Response.Builder readResponseHeaders(boolean expectContinue) throws IOException;

  /** Returns a stream that reads the response body. */
  ResponseBody openResponseBody(Response response) throws IOException;

  /**
   * Cancel this stream. Resources held by this stream will be cleaned up, though not synchronously.
   * That may happen later by the connection pool thread.
   */
  void cancel();
}
```
1. writeRequestHeaders(Request request) ：写入请求头
1. createRequestBody(Request request, long contentLength) ：写入请求体
1. flushRequest() 相当于flush,把请求刷入底层socket
1. finishRequest() throws IOException : 相当于flush，把请求输入底层socket并不在发出请求
1. readResponseHeaders(boolean expectContinue)：读取响应头
1. openResponseBody(Response response)：读取响应体
1. void cancel() ：取消请求

实现类：
1. Http1Codec
2. Http2Codec

#### 参考

[OkHttp源码解析(八):OKHttp中阶之连接与请求值前奏](https://www.jianshu.com/p/510b27237c76)