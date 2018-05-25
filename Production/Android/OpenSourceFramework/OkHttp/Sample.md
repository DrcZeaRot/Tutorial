### 请求示例

1. 同步请求，以Get方法为例
    ```
    OkHttpClient client = new OkHttpClient.Builder()//Builder模式构建实例
        .addInterceptor(DefaultLogInterceptor())//通过拦截器，方便扩展
        .build();//构建Client，实际包办请求
    Request request = new Request.Builder()
      .url(url)
      .build();//构建Request
    Call call = client.newCall(request);//创建Request包装类Call
    Response response = call.execute();//同步请求，经过拦截器，获得最终Response
    ```

2. 异步请求，以Post方法为例
    ```
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    String json = "......";
    OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(DefaultLogInterceptor())
        .build();
    RequestBody body = RequestBody.create(JSON, json);
    Request request = new Request.Builder()
        .url(url)
        .post(body)
        .build();
    Call call = client.newCall(request);
    Response response = call.enqueue(new CallBack(){
            @Override public void onResponse(Call<T> call, Response<T> response){
                ...
            }

            @Override public void onFailure(Call<T> call, Throwable t) {
                ...
            }
        })
    ```