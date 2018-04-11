### 解析

* 解析阶段是：虚拟机将常量池内的符号引用，替换为直接引用的过程
    * 符号引用见[Class文件格式](../../ClassFileStructure/CFS_Intro.md)
    * Class文件中，它以CONSTANT_Class_info、CONSTANT_Fieldref_info、CONSTANT_Methodref_info等类型的常量出现
* 解析阶段中，所说的"直接引用"与"符号引用"又有什么关联呢：
    1. 符号引用(Symbolic References)：
        * 符号引用以一组符号来描述所引用的目标
        * 符号引用可以使任何形式的字面量，只要使用时能无歧义地定位到目标即可
        * 符号引用与虚拟机实现的内存布局无关，引用的目标并不一定已经加载到内存中
        * 各种虚拟机实现的内存布局可以各不相同
            * 但是他们能接受的符号引用必须都是一致的
            * 因为符号引用的字面量形式，明确定义在JVM规范的Class文件格式中
    2. 直接引用(Direct References)：
        * 直接引用可以是：
            1. 直接指向目标的指针
            2. 相对偏移量
            3. 一个能间接定位到目标的句柄
        * 直接引用是和虚拟机实现的内存布局相关的
            * 同一个符号引用，在不同虚拟机实例上，翻译出来的直接引用，一般不会相同
        * 如果有了直接引用，那引用的目标，必定已经在内存中存在。

##### 解析发生时刻

* 虚拟机规范中并没有规定解析阶段发生的具体时间，
* 只要求了在
    * anewarray/checkcast/getfield/getstatic/instanceof/invokedynamic
    * invokeinterface/invokespecial/invokestatic/invokevirtual
    * ldc/ldc_w/multianewarray/new/putfield/putstatic
    * 以上16个用于操作符号引用的字节码指令之前
    * 先对他们所使用的符号引用进行解析

##### 解析动作的目标

```
解析动作主要针对类/接口、字段、类方法、接口方法、方法类型、方法句柄、调用点限定符7类符号引用进行
```

[解析过程过于冗长，见《深入理解Java虚拟机》7.3.4节]()