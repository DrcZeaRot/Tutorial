### 请求/响应数据Mapping

##### Converter的作用

举例：比如你声明了一个如下的方法，其返回值为RxJava的Observable<XXX>

```
interface MyRetrofitService{

    @Post("/xxx/xx/x")
    fun getSthFromGithub(@QueryMap params : Map<String,String>) : Observable<ApiResult<Something>>
}
```

默认只会返回一个ResponseBody

如果你想获得一个任意的POJO类(比如这个ApiResult<XXX>)，就需要指定一个相关功能的Converter

##### Converter定义

```
public interface Converter<F, T> {
    //实际上就是一个Mapper，将F映射为T
  T convert(F value) throws IOException;

    //Converter工厂
  abstract class Factory {
    //ResponseBody的转换器
    public @Nullable Converter<ResponseBody, ?> responseBodyConverter(Type type,
        Annotation[] annotations, Retrofit retrofit) {
      return null;
    }
    //RequestBody的转换器
    public @Nullable Converter<?, RequestBody> requestBodyConverter(Type type,
        Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
      return null;
    }

    public @Nullable Converter<?, String> stringConverter(Type type, Annotation[] annotations,
        Retrofit retrofit) {
      return null;
    }

    protected static Type getParameterUpperBound(int index, ParameterizedType type) {
      return Utils.getParameterUpperBound(index, type);
    }

    protected static Class<?> getRawType(Type type) {
          return Utils.getRawType(type);
        }
      }
}
```