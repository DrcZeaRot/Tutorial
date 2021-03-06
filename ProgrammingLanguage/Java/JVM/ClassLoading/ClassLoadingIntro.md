### 类加载机制简介

虚拟机的类加载机制是：
* 虚拟机把描述类的数据，从Class文件加载到内存
* 并对数据进行校验、转换解析和初始化
* 最终形成可以被虚拟机直接使用的Java类型

与那些在编译时需要进行连接工作的语言不同：
* Java语言里，类型的加载、连接和初始化过程，都是在程序运行期间完成的
* 这种策略虽然会令类加载时稍微增加一些性能开销
* 但是会为Java应用程序提供高度的灵活性
* Java里天生可以动态扩展的语言特性，就是依赖运行期动态加载和动态连接这个特点实现的