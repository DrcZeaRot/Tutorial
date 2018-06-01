### RouteSelector

类注释：
```
选择连接到服务器的路由，
每个连接应该是：代理服务器/IP地址/TLS模式 三者中的一种。
连接可以被回收
```

* 路由选择器存在的意义：
    ```
    因为HTTP请求处理过程中所需的TCP连接建立过程，
    主要是找到一个Route，然后依据代理协议规则与特定目标建立TCP连接。
    1. 对于无代理的情况：是与HTTP服务器建立TCP连接
    2. 对于SOCKS代理和http代理：是与代理服务器建立tcp连接
    虽然都是与代理服务器建立tcp连接，但是SOCKS代理协议和http代理协议又有一定的区别
    ```
    ```
    借助于域名做负均衡已经是网络中非常常见的手法了，
    因而，常常会有域名对应不同IP地址的情况。
    同时相同系统也可以设置多个代理，这使Route的选择变得非常复杂。

    在OKHTTP中，对Route连接有一定的错误处理机制。
    OKHTTP会逐个尝试找到Route建立TCP连接，直到找到可用的哪一个。
    这样对Route信息有良好的管理。
    OKHTTP中借助RouteSelector类管理所有路由信息，并帮助选择路由。
    ```
1. 字段、构造器
    ```
      private final Address address;
      private final RouteDatabase routeDatabase;
      private final Call call;
      private final EventListener eventListener;

      /* State for negotiating the next proxy to use. */
      private List<Proxy> proxies = Collections.emptyList();
      private int nextProxyIndex;

      /* State for negotiating the next socket address to use. */
      private List<InetSocketAddress> inetSocketAddresses = Collections.emptyList();

      /* State for negotiating failed routes */
      private final List<Route> postponedRoutes = new ArrayList<>();

      public RouteSelector(Address address, RouteDatabase routeDatabase, Call call,
          EventListener eventListener) {
        this.address = address;
        this.routeDatabase = routeDatabase;
        this.call = call;
        this.eventListener = eventListener;

        resetNextProxy(address.url(), address.proxy());
      }

       /** Prepares the proxy servers to try. */
       private void resetNextProxy(HttpUrl url, Proxy proxy) {
         if (proxy != null) {
           // If the user specifies a proxy, try that and only that.
           proxies = Collections.singletonList(proxy);
         } else {
           // Try each of the ProxySelector choices until one connection succeeds.
           List<Proxy> proxiesOrNull = address.proxySelector().select(url.uri());
           proxies = proxiesOrNull != null && !proxiesOrNull.isEmpty()
               ? Util.immutableList(proxiesOrNull)
               : Util.immutableList(Proxy.NO_PROXY);
         }
         nextProxyIndex = 0;
       }
    ```
    * 如果构建OkHttpClient时指定了Proxy，就是用这些Proxy
    * 如果没有，则使用ProxySelector(默认是DefaultProxySelector)对适合URL的Proxy进行选择
    * 如果不想使用默认的Proxy，可以在构建Client时配置Proxy或ProxySelector
3. hasNext & next
    ```
      public boolean hasNext() {
        return hasNextProxy() || !postponedRoutes.isEmpty();
      }

      private boolean hasNextProxy() {
        return nextProxyIndex < proxies.size();
      }

      public Selection next() throws IOException {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        // Compute the next set of routes to attempt.
        List<Route> routes = new ArrayList<>();
        while (hasNextProxy()) {
          // Postponed routes are always tried last. For example, if we have 2 proxies and all the
          // routes for proxy1 should be postponed, we'll move to proxy2. Only after we've exhausted
          // all the good routes will we attempt the postponed routes.
          Proxy proxy = nextProxy();
          for (int i = 0, size = inetSocketAddresses.size(); i < size; i++) {
            Route route = new Route(address, proxy, inetSocketAddresses.get(i));
            if (routeDatabase.shouldPostpone(route)) {
              postponedRoutes.add(route);
            } else {
              routes.add(route);
            }
          }

          if (!routes.isEmpty()) {
            break;
          }
        }

        if (routes.isEmpty()) {
          // We've exhausted all Proxies so fallback to the postponed routes.
          routes.addAll(postponedRoutes);
          postponedRoutes.clear();
        }

        return new Selection(routes);
      }
    ```
    1. hasNextProxy：调用nextProxy方法，会更新nextProxyIndex，影响hasNextProxy的结果
    2. postponedRoutes：
        1. 获取nextProxy方法，还会resetNextInetSocketAddress(proxy)
            1. 将inetSocketAddresses重新指向一个空的ArrayList
            2. 当前Proxy是SOCKS类型，则向inetSocketAddresses添加一个新的元素
            3. 通过Address中的DNS对host进行查询，获取一个InetAddress的列表
                * 遍历列表，添加元素到inetSocketAddresses
        2. 在next方法中，获取nextProxy后，遍历inetSocketAddresses
            1. 对每个InetSocketAddress创建Route
            2. RouteDatabase判断Route是否需要shouldPostpone
            3. 如果需要，将route添加到postponedRoutes列表中
            4. 否则将route添加到局部的routes列表中
            5. 如果hasNextProxy遍历完所有的Proxy，发现局部的routes列表为空
                * 添加postponedRoutes到局部列表routes
                * 清空postponedRoutes列表
            6. 创建Selection(routes)
4. Route列表包装：Selection类
    ```
    public static final class Selection {
        private final List<Route> routes;
        private int nextRouteIndex = 0;

        Selection(List<Route> routes) {
          this.routes = routes;
        }

        public boolean hasNext() {
          return nextRouteIndex < routes.size();
        }

        public Route next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          return routes.get(nextRouteIndex++);
        }

        public List<Route> getAll() {
          return new ArrayList<>(routes);
        }
      }
    ```

#### 参考

[OkHttp源码解析(四):OKHttp中阶之拦截器及调用链](https://www.jianshu.com/p/e3b6f821acb8)