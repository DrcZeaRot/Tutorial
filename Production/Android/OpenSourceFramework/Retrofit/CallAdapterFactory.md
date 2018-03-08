### 响应数据类型适配

##### CallAdapter的作用

举例：比如你声明了一个如下的方法，其返回值为RxJava的Observable<XXX>

```
interface MyRetrofitService{

    @Post("/xxx/xx/x")
    fun getSthFromGithub(@QueryMap params : Map<String,String>) : Observable<ApiResult<Something>>
}
```

```
如果：接下来用于创建这个Service实例的Retrofit实例，并没有设置可以对Observable进行适配的CallAdapter
则此方法无法顺利执行，并抛出"Could not locate call adapter for XXX"异常。
```

##### CallAdapter定义

```
public interface CallAdapter<R, T> {
    //决定此Adapter针对的Type
  Type responseType();
    //适配过程，Call => T
  T adapt(Call<R> call);

    //Adapter工厂
  abstract class Factory {
        //根据不同的returnType、annotation，创建不同的CallAdapter
      public abstract @Nullable CallAdapter<?, ?> get(Type returnType, Annotation[] annotations,
          Retrofit retrofit);

      protected static Type getParameterUpperBound(int index, ParameterizedType type) {
        return Utils.getParameterUpperBound(index, type);
      }

      protected static Class<?> getRawType(Type type) {
        return Utils.getRawType(type);
      }
    }

}
```