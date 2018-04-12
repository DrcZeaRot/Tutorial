### JVM


##### 运行时数据区域
![运行时数据区](img/RuntimeDataArea.png)

* [程序计数器](RuntimeDataArea/PC.md)
* [Java虚拟机栈](RuntimeDataArea/JvmStacks.md)
* [本地方法栈](RuntimeDataArea/NativeMethodStack.md)
* [Java堆](RuntimeDataArea/JavaHeap.md)
* [方法区](RuntimeDataArea/MethodArea.md)
* [运行时常量池](RuntimeDataArea/RuntimeConstantPool.md)
* [直接内存](RuntimeDataArea/DirectMemory.md)

##### HotSpot虚拟机-Java堆-对象

* [对象的创建](ObjectInfo/ObjectCreation.md)
* [对象的内存布局](ObjectInfo/ObjectMemoryLayout.md)
* [对象的访问定位](ObjectInfo/ObjectAccessLocal.md)

##### 垃圾收集器与内存分配策略

* JVM如何判断对象死亡：
    * [引用计数算法](GC/ReferenceCounting.md)
    * [可达性分析算法](GC/ReachabilityAnalysis.md)
    * [引用的细节](GC/Reference.md)
    * [生存还是死亡](GC/Finalize.md)
* [垃圾收集算法](GC/GarbageCollector.md)
* [内存分配与回收策略](GC/MemoryStrategy.md)

##### 类文件结构

* [简介](ClassFileStructure/CFS_Intro.md)
* [魔数与class文件的版本](ClassFileStructure/CFS_MagicNumber&Version.md)
* [常量池](ClassFileStructure/CFS_ConstantPool.md)
* [访问标志](ClassFileStructure/CFS_AccessFlags.md)
* [类索引、父类索引与接口索引集合](ClassFileStructure/CFS_IndexList.md)
* [字段表集合](ClassFileStructure/CFS_FieldList.md)
* [方法表集合](ClassFileStructure/CFS_MethodList.md)
* [属性表集合](ClassFileStructure/CFS_AttributeList.md)

##### 虚拟机类加载机制

* [类加载简介](ClassLoading/ClassLoadingIntro.md)
* [类加载的时机](ClassLoading/ClassLoadingTiming.md)
* [类加载的过程](ClassLoading/ClassLoadingProcess.md)
* 类加载器：
    1. [类加载器与双亲委托](ClassLoading/ClassLoader.md)
    2. [Android的类加载器](ClassLoading/ClassLoaderInAndroid.md)

##### 虚拟机字节码执行引擎

* 方法调用
    * [方法解析](ByteCodeEngine/MethodResolution.md)
    * [方法分派](ByteCodeEngine/MethodDispatch.md)

#### 参考

[深入理解Java虚拟机：JVM高级特性与最佳实践.pdf]()

[JVM内幕：Java虚拟机详解](http://www.importnew.com/17770.html)

[深入理解JVM—JVM内存模型](http://www.cnblogs.com/dingyingsi/p/3760447.html)

[JVM 内存初学 (堆(heap)、栈(stack)和方法区(method) )](http://www.cnblogs.com/dingyingsi/p/3760730.html)

[垃圾收集算法](https://github.com/LRH1993/android_interview/blob/master/java/virtual-machine/Garbage-Collector.md)

[【深入Java虚拟机】之一：Java内存区域与内存溢出](https://blog.csdn.net/ns_code/article/details/17565503)