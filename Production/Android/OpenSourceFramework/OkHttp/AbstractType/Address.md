### Address

类注释：
```
与服务器连接的格式，对于简单的链接，这里是服务器的主机名和端口号。
如果是通过代理(Proxy)的链接，则包含代理信息(Proxy)。
如果是安全链接，则还包括SSL socket Factory、hostname验证器，证书等。
```

* 在RetryAndFollowUpInterceptor::intercept方法中被实例化
    ```
      private Address createAddress(HttpUrl url) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (url.isHttps()) {
          sslSocketFactory = client.sslSocketFactory();
          hostnameVerifier = client.hostnameVerifier();
          certificatePinner = client.certificatePinner();
        }

        return new Address(url.host(), url.port(), client.dns(), client.socketFactory(),
            sslSocketFactory, hostnameVerifier, certificatePinner, client.proxyAuthenticator(),
            client.proxy(), client.protocols(), client.connectionSpecs(), client.proxySelector());
      }
    ```
* 字段表
    ```
      final HttpUrl url;
      final Dns dns;
      final SocketFactory socketFactory;
      final Authenticator proxyAuthenticator;
      final List<Protocol> protocols;
      final List<ConnectionSpec> connectionSpecs;
      final ProxySelector proxySelector;
      final @Nullable Proxy proxy;
      final @Nullable SSLSocketFactory sslSocketFactory;
      final @Nullable HostnameVerifier hostnameVerifier;
      final @Nullable CertificatePinner certificatePinner;
    ```
* 连接池复用
    ```
      boolean equalsNonHost(Address that) {
        return this.dns.equals(that.dns)
            && this.proxyAuthenticator.equals(that.proxyAuthenticator)
            && this.protocols.equals(that.protocols)
            && this.connectionSpecs.equals(that.connectionSpecs)
            && this.proxySelector.equals(that.proxySelector)
            && equal(this.proxy, that.proxy)
            && equal(this.sslSocketFactory, that.sslSocketFactory)
            && equal(this.hostnameVerifier, that.hostnameVerifier)
            && equal(this.certificatePinner, that.certificatePinner)
            && this.url().port() == that.url().port();
      }
    ```
    * 只有两个Address相同才能说明这两个连接的配置信息是一直的，才能使用RealConnection的复用
