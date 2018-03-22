### 代理模式（Proxy Pattern）

简介：为其他对象提供一个代理以控制对这个对象的访问。

优缺点：
* 优点
    1. 职责清晰：具体Subject只负责实现业务逻辑，后期由代理进行其他非本职的工作
	2. 高扩展：具体的Subject是随时变化的，但不论它怎么变，代理都不用修改直接使用(因为是面向接口的)
	3. 智能化(详见动态代理)

##### 静态代理

* 普通代理
```
需求：客户端只能访问代理角色，不能访问真实角色
```

* 强制代理
```
需求：只有代理能才能进行操作

强制代理的概念就是要从真实角色查找到代理角色， 不允许直接访问真实角色
```

* 功能性代理
```
概念：
代理类不仅仅可以实现主题接口， 也可以实现其他接口完成不同的任务
而且代理的目的是在目标对象方法的基础上作增强， 这种增强的本质通常就是对目标对象的方法进行拦截和过滤
```


[静态代理代码示例](../../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/designpattern/structure/proxy/StaticProxy.kt)

##### 动态代理
```
概念：
动态代理是在实现阶段不用关心代理谁， 而在运行阶段才指定代理哪一个对象
相对来说， 自己写代理类的方式就是静态代理
```

```
JDK提供了Proxy和InvocationHandler帮助实现动态代理
```


[动态代理代码示例](../../../../TutorialCodeSample/src/main/java/com/xcstasy/tutorial/designpattern/structure/proxy/DynamicProxy.kt)