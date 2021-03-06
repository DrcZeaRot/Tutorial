### 验证

验证是连接阶段的第一步，这一阶段的目的是：
1. 为了确保Class文件的字节流中包含的信息，符合当前虚拟机的要求
2. 并且不会危害虚拟机自身的安全。

整体来看，验证阶段大致上会完成以下4个阶段的校验动作：
1. 文件格式验证
2. 元数据验证
3. 字节码验证
4. 符号引用验证

##### 文件格式验证
* 第一阶段要验证：字节流是否符合Class文件格式的规范
* 并且能被当前版本的虚拟机处理
* 此阶段可能包括以下验证点：
    1. 是否以魔数0xCAFEBABE开头
    2. 主、次版本号是否在当前虚拟机处理范围之内
    3. 常量池的常量中是否有不被支持的常量类型(检查常量tag标志)
    4. 指向常量的各种索引值中是否有指向不存在的常量或不符合类型的常量
    5. CONSTANT_Utf8_info型的常量中是否有不符合UTF8编码的数据
    6. Class文件中各个部分及文件本身是否有被删除的或附加的其他信息
    7. 等等
* 通过这个阶段的验证后，字节流才会进入内存的方法区中进行存储
    * 后面的3个验证阶段全部是基于方法区的存储结构进行
    * 不会再直接操作字节流
##### 元数据验证
* 第二阶段是：对字节码描述的信息进行语义分析，以保证其描述的信息复核Java语言规范的要求
* 可能包括的验证点：
    1. 这个类是否有父类(java.lang.Object之外，其他的类都应该有父类)
    2. 这个类的父类，是否继承了不允许被继承的类(final类)
    3. 如果这个类不是抽象类，是否实现了其父类/接口之中要求实现的所有方法
    4. 类中的字段、方法是否与父类自产生矛盾
        * 覆盖了父类的final字段
        * 出现不符合规则的方法重载：方法参数都一致，但返回值类型却不同等
##### 字节码验证
* 第三阶段是整个验证过称重最复杂的一个阶段
* 主要目的是：通过数据流和控制流分析，确定程序语义是合法的、复核逻辑的
* 在第二段对元数据信息中的数据类型昨晚校验后
    * 这个阶段将会对类的方法体进行校验分析
    * 保证被校验类的方法，在运行时不会做出危害虚拟机安全的事件
* 验证点有：
    1. 保证任意时刻，操作数栈的数据类型与指令代码序列都能配合工作
        * 如不会出现：在操作栈纺织类一个int类型的数据，使用时却按long类型来加载进入本地变量表
    2. 保证跳转指令不会跳转到方法体意外的字节码指令上
    3. 保证方法体中的类型转换是有效地
        * 如：可以把一个子类对象赋值给父类数据类型，这是安全的
        * 但是把父类对象赋值给子类数据类型、甚至把对象赋值给与它毫无继承关系、完全不相干的一个数据类型，则是危险、不合法的。
* 如果一个类，方法体字节码没有通过字节码验证，那肯定是有问题的
* 但即使方法体通过了字节码验证，也不能说明其一定就是安全的
##### 符号引用验证
* 最后一个阶段的校验发生在虚拟机将符号引用转化为直接引用的时候
* 这个转化动作将在连接的第三阶段——解析阶段中发生
* 符号引用验证可以看做是：对类自身以外(常量池中的各种符号引用)的信息进行匹配性校验
* 验证点有：
    1. 符号引用中通过字符串描述的全限定名是否能找到对应的类
    2. 在指定类中是否存在符合方法的字段描述符，以及简单名称所描述的方法和字段
    3. 符号引用中的类、字段、方法的访问性(private、protected、public、default)是否可被当前类访问
    4. 等等
* 符号引用验证的目的：确保解析动作能正常执行
    * 如果无法通过符号引用验证，将抛出一个java.lang.IncompatibleClassChangeError异常的子类
        * 如：IllegalAccessError、NoSuchFieldError、NoSuchMethodError等
##### 其他

* 对于虚拟机的类加载机制来说，验证阶段是一个非常重要的、但不是一定必要的阶段(对程序运行期没有影响)
* 如果所运行的全部代码(包括自己编写的以及第三方包中的代码)都已经被反复使用、验证过
* 在实施阶段可考虑使用"-Xverify:none"参数来关闭大部分的类验证措施，缩短虚拟机类加载的时间。
