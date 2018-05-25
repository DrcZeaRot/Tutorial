### Request&Response

1. Request、Response分别抽象成请求和相应
2. Request内容如下：
    ```
    final HttpUrl url;
    final String method;
    final Headers headers;
    final @Nullable RequestBody body;
    final Object tag;

    private volatile CacheControl cacheControl; // Lazily initialized.
    ```
    * RequestBody是abstract的，官方实现的子类是有FormBody (表单提交的)和 MultipartBody(文件上传)，分别对应了两种不同的MIME类型
    1. FormBody ："application/x-www-form-urlencoded"
    2. MultipartBody："multipart/"+xxx.
3. 其中Response内容如下：
    ```
    final Request request;
    final Protocol protocol;
    final int code;
    final String message;
    final @Nullable Handshake handshake;
    final Headers headers;
    final @Nullable ResponseBody body;
    final @Nullable Response networkResponse;
    final @Nullable Response cacheResponse;
    final @Nullable Response priorResponse;
    final long sentRequestAtMillis;
    final long receivedResponseAtMillis;

    private volatile CacheControl cacheControl; // Lazily initialized.
    ```
    * ResponseBody是abstract的，官方实现的子类也是有两个:RealResponseBody和CacheResponseBody,分别代表真实响应和缓存响应。
4. 由于RFC协议规定，所以头部信息不是随便写的，request的header与response的header的标准都不同。
    * 具体的见 [List of HTTP header fields](https://en.wikipedia.org/wiki/List_of_HTTP_header_fields)。
    1. OKHttp的封装类Request和Response为了应用程序编程方便，会把一些常用的Header信息专门提取出来，作为局部变量。
    2. 比如contentType，contentLength，code,message,cacheControl,tag...它们其实都是以name-value对的形势，存储在网络请求的头部信息中。