### StreamAllocation


#### 方法解析

1. newStream
    1. 创建RealConnection；当前Request是GET时，不进行HealthCheck。
            ```
              public HttpCodec newStream(
                  OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
                ...
                try {
                  RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout,
                      writeTimeout, pingIntervalMillis, connectionRetryEnabled, doExtensiveHealthChecks);
                  HttpCodec resultCodec = resultConnection.newCodec(client, chain, this);

                  synchronized (connectionPool) {
                    codec = resultCodec;
                    return resultCodec;
                  }
                } catch (IOException e) { ... }
              }

              /**
               * Finds a connection and returns it if it is healthy. If it is unhealthy the process is repeated
               * until a healthy connection is found.
               */
              private RealConnection findHealthyConnection(int connectTimeout, int readTimeout,
                  int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled,
                  boolean doExtensiveHealthChecks) throws IOException {
                while (true) {
                  RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout,
                      pingIntervalMillis, connectionRetryEnabled);

                  // If this is a brand new connection, we can skip the extensive health checks.
                  synchronized (connectionPool) {
                    if (candidate.successCount == 0) {
                      return candidate;
                    }
                  }

                  // Do a (potentially slow) check to confirm that the pooled connection is still good. If it
                  // isn't, take it out of the pool and start again.
                  if (!candidate.isHealthy(doExtensiveHealthChecks)) {
                    noNewStreams();
                    continue;
                  }

                  return candidate;
                }
              }
            ```
        2. findConnection：
            ```
              /**
               * Returns a connection to host a new stream. This prefers the existing connection if it exists,
               * then the pool, finally building a new connection.
               */
              private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout,
                  int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
                boolean foundPooledConnection = false;
                RealConnection result = null;
                Route selectedRoute = null;
                Connection releasedConnection;
                Socket toClose;
                synchronized (connectionPool) {
                  if (released) throw new IllegalStateException("released");
                  if (codec != null) throw new IllegalStateException("codec != null");
                  if (canceled) throw new IOException("Canceled");

                  // Attempt to use an already-allocated connection. We need to be careful here because our
                  // already-allocated connection may have been restricted from creating new streams.
                  releasedConnection = this.connection;
                  toClose = releaseIfNoNewStreams();
                  if (this.connection != null) {
                    // We had an already-allocated connection and it's good.
                    result = this.connection;
                    releasedConnection = null;
                  }
                  if (!reportedAcquired) {
                    // If the connection was never reported acquired, don't report it as released!
                    releasedConnection = null;
                  }

                  if (result == null) {
                    // Attempt to get a connection from the pool.
                    Internal.instance.get(connectionPool, address, this, null);
                    if (connection != null) {
                      foundPooledConnection = true;
                      result = connection;
                    } else {
                      selectedRoute = route;
                    }
                  }
                }
                closeQuietly(toClose);

                if (releasedConnection != null) {
                  eventListener.connectionReleased(call, releasedConnection);
                }
                if (foundPooledConnection) {
                  eventListener.connectionAcquired(call, result);
                }
                if (result != null) {
                  // If we found an already-allocated or pooled connection, we're done.
                  return result;
                }

                // If we need a route selection, make one. This is a blocking operation.
                boolean newRouteSelection = false;
                if (selectedRoute == null && (routeSelection == null || !routeSelection.hasNext())) {
                  newRouteSelection = true;
                  routeSelection = routeSelector.next();
                }

                synchronized (connectionPool) {
                  if (canceled) throw new IOException("Canceled");

                  if (newRouteSelection) {
                    // Now that we have a set of IP addresses, make another attempt at getting a connection from
                    // the pool. This could match due to connection coalescing.
                    List<Route> routes = routeSelection.getAll();
                    for (int i = 0, size = routes.size(); i < size; i++) {
                      Route route = routes.get(i);
                      Internal.instance.get(connectionPool, address, this, route);
                      if (connection != null) {
                        foundPooledConnection = true;
                        result = connection;
                        this.route = route;
                        break;
                      }
                    }
                  }

                  if (!foundPooledConnection) {
                    if (selectedRoute == null) {
                      selectedRoute = routeSelection.next();
                    }

                    // Create a connection and assign it to this allocation immediately. This makes it possible
                    // for an asynchronous cancel() to interrupt the handshake we're about to do.
                    route = selectedRoute;
                    refusedStreamCount = 0;
                    result = new RealConnection(connectionPool, selectedRoute);
                    acquire(result, false);
                  }
                }

                // If we found a pooled connection on the 2nd time around, we're done.
                if (foundPooledConnection) {
                  eventListener.connectionAcquired(call, result);
                  return result;
                }

                // Do TCP + TLS handshakes. This is a blocking operation.
                result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis,
                    connectionRetryEnabled, call, eventListener);
                routeDatabase().connected(result.route());

                Socket socket = null;
                synchronized (connectionPool) {
                  reportedAcquired = true;

                  // Pool the connection.
                  Internal.instance.put(connectionPool, result);

                  // If another multiplexed connection to the same address was created concurrently, then
                  // release this connection and acquire that one.
                  if (result.isMultiplexed()) {
                    socket = Internal.instance.deduplicate(connectionPool, address, this);
                    result = connection;
                  }
                }
                closeQuietly(socket);

                eventListener.connectionAcquired(call, result);
                return result;
              }
            ```
            1. 首先尝试使用已经定位到的连接(要注意:可能这个连接已经被限制创建新的Stream)
                1. 如果当前StreamAllocation的connection还未找到，尝试从连接池中获取连接
                2. 上述流程中，如果找到连接，证明已有连接被成功创建过，return
            2. 第二轮尝试配合路由、从连接池中寻找可用连接
                1. 判断是否需要选择路由，如果需要则进行阻塞选择
                2. 尝试配合路由，从连接池中获取可用连接
                3. 如果找到了，return
                4. 如果没获取到，则创建RealConnection实例，走第3步
            3. 如果第二轮，仍未从连接池中找到可用连接，则通过Connection类进行连接
                1. RealConnection::connect，进行阻塞的TCP+TLS握手
                1. 将成功连接的route添加到RouteDatabase中
                1. 将成功的Connection添加到连接池中
                1. 如果指向同一个Address的另一个多路复用的连接并发地被创建，则释放当前连接，使用多路复用的连接。
        3. 根据Connection类型(Http1/Http2)，创建对应的HttpCodec实例
            ```
              public HttpCodec newCodec(OkHttpClient client, Interceptor.Chain chain,
                  StreamAllocation streamAllocation) throws SocketException {
                if (http2Connection != null) {
                  return new Http2Codec(client, chain, streamAllocation, http2Connection);
                } else {
                  socket.setSoTimeout(chain.readTimeoutMillis());
                  source.timeout().timeout(chain.readTimeoutMillis(), MILLISECONDS);
                  sink.timeout().timeout(chain.writeTimeoutMillis(), MILLISECONDS);
                  return new Http1Codec(client, streamAllocation, source, sink);
                }
              }
            ```

#### 参考

[OKHttp源码解析(九):OKHTTP连接中三个"核心"RealConnection、ConnectionPool、StreamAllocation](https://www.jianshu.com/p/6166d28983a2)