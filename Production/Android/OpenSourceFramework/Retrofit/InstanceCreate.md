### Retrofit创建Interface实例

>议题：Retrofit如何通过retrofit.create(Service::class.java)获得Service实例？

关键代码：

* retrofit.create(class)

```
public <T> T create(final Class<T> service) {
    Utils.validateServiceInterface(service);
    if (validateEagerly) {//在构建Retrofit实例时，控制此属性
    //如果为True，则在service对象构建前，提前加载serviceMethod
      eagerlyValidateMethods(service);
    }
    //通过JDK的Proxy与InvocationHandler实现动态代理
    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
        new InvocationHandler() {
        //获取当前Platform，分为：Android、Java8、Platform
          private final Platform platform = Platform.get();

          @Override public Object invoke(Object proxy, Method method, @Nullable Object[] args)
              throws Throwable {
            // If the method is a method from Object then defer to normal invocation.
            if (method.getDeclaringClass() == Object.class) {
              return method.invoke(this, args);
            }
            //Java8平台有可能有Default，其余此处都会是false
            if (platform.isDefaultMethod(method)) {
              return platform.invokeDefaultMethod(method, service, proxy, args);
            }
            //加载serviceMethod
            ServiceMethod<Object, Object> serviceMethod =
                (ServiceMethod<Object, Object>) loadServiceMethod(method);
            //构建OkHttpCall(包装了OkHttp3的Call)
            OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args);
            //使用对应的CallAdapter对call进行适配
            return serviceMethod.callAdapter.adapt(okHttpCall);
          }
        });
  }
```

* retrofit.eagerlyValidateMethods
```
private void eagerlyValidateMethods(Class<?> service) {
    Platform platform = Platform.get();
    for (Method method : service.getDeclaredMethods()) {
      if (!platform.isDefaultMethod(method)) {
      //提前加载serviceMethod
        loadServiceMethod(method);
      }
    }
  }
```

* retrofit.loadServiceMethod
```
private final Map<Method, ServiceMethod<?, ?>> serviceMethodCache = new ConcurrentHashMap<>();
//对serviceMethod进行同步的缓存、获取
ServiceMethod<?, ?> loadServiceMethod(Method method) {
    ServiceMethod<?, ?> result = serviceMethodCache.get(method);
    if (result != null) return result;

    synchronized (serviceMethodCache) {
      result = serviceMethodCache.get(method);
      if (result == null) {
      //此处ServiceMethod的构建会直接对Converter、CallAdapter进行匹配
        result = new ServiceMethod.Builder<>(this, method).build();
        serviceMethodCache.put(method, result);
      }
    }
    return result;
  }
```