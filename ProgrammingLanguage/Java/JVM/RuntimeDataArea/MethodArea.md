### 方法区(Method Area)

* 方法区与Java堆一样，是各个线程共享的内存区域，它用于存储已经被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码的等数据。
* 虽然JVM规范把方法去描述为堆上的一个逻辑部分，但是它却有一个别名叫做Non-Heap(非堆)，目的应该是与Java堆区分开。

##### 永久代

* 对于习惯在HotSpot虚拟机上开发、部署程序的开发者来说，很多人都更愿意把方法去称为"永久代(Permanent Generation)"
* 本质上，两者并不等价，仅仅是因为HotSpot虚拟机的设计团队选择把GC分代收集扩展至方法去，或者说使用永久代来实现方法去而已
* 这样HotSpot的垃圾收集器可以像管理Java堆一样管理这部分内存，能够省去专门为方法区编写内存管理代码的工作。
* 对于其他虚拟机来说，是不存在永久代的概念的。

>原则上，如何实现方法区术语虚拟机实现细节，不受JVM规范约束

但是用永久代来实现方法区，现在看来并不是个好主意：
* 这样更容易遇到内存溢出问题
* 极少数方法(如String.intern())会因为这个原因导致不同虚拟机下有不同的表现
* 因此：对于HotSpot虚拟机，根据官方发布的路线图信息，现在也有放弃永久代并逐步改为采用NativeMemory来实现方法区的规划
* 已发布的JDK7的HotSpot中，已经把原本放在永久代的字符串常量池移出。

##### JVM规范
JVM规范对方法区的限制非常宽松，除了和Java堆一样不需要连续地内存和可以选择固定大小或者可扩展外，还可以选择不识闲垃圾收集。

##### 其他
根据Java虚拟机规范的规定，当方法区无法满足内存分配需求时，将抛出OutOfMemoryError异常