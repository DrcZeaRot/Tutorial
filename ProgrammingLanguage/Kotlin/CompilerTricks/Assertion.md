
##### JVM Compiler Args

> kotlin.jvm.internal.Intrinsics.java中有一系列的断言方法，这些方法会被KotlinCompiler生成，并插入到字节码中需要被检查的位置

常见检查：
1. lateinit var：
2. 方法入参
3. 传递给需要非空参数方法的变量

```
    public static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
    public static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
    public static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
    public static void throwUninitializedProperty(java.lang.String);
    public static void throwUninitializedPropertyAccessException(java.lang.String);
    public static void throwNpe(...);
```

解决方法：
1. 使用混淆(绝对可行)
    * -assumenosideeffects语句来忽视指定方法
2. 添加编译器选项(目前Android试验无效)
    * -Xno-call-assertions
    * -Xno-param-assertions
    * -Xno-receiver-assertions

详细见：

[K2JVMCompilerArguments](https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JVMCompilerArguments.kt)


##### Common Compiler Args

[Contracts DSL](https://aisia.moe/2018/03/25/kotlin-contracts-dsl/)

* -Xeffect-system
* -Xread-deserialized-contracts
* -Xallow-kotlin-package

[CommonCompilerArguments](https://github.com/JetBrains/kotlin/blob/d6792f5f521ca598d9040e24c896e21dac12656c/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/CommonCompilerArguments.kt)

