### 原子性、可见性与有序性

> Java内存模型是围绕着并发过程中，如何处理<原子性>、<可见性>和<有序性>这3个特征来建立的。

##### 原子性(Atomicity)：
* 由Java内存模型来直接保证的原子性变量操作包括：read、load、assign、use、store和write
* 我们大致可以认为：基本数据类型的访问、读写是具备原子性的
    > 例外就是long和double的非原子性协定，但没必要在意这个几乎不会发生的例外。
* 如果应用场景需要一个更大范围的原子性保证(经常遇到)
    * Java内存模型还提供了lock和unlock操作来满足这种需求
    * 尽管虚拟机未把lock和unlock、操作直接开发给用户使用
        1. 但却提供了更高层次的字节码指令：monitorenter和monitorexit来隐式地使用这两个操作
        2. 这两个字节码指令反映到Java代码中，就是同步块——synchronized关键字
        3. 因此在synchronized块之间的操作也具备原子性
##### 可见性(Visibility)：
* 可见性是指：当前一个线程修改了共享变量的值，其他线程能够立即得知这个修改(见[volatile](Volatile.md))
* Java内存模型是通过：
    1. 在变量修改后，将新值同步回主内存
    2. 在变量读取前，从主内存刷新变量值
    3. 上述这种依赖主内存作为传递媒介的方式来实现可见性的。
    4. 无论普通变量还是volatile变量都是如此，普通变量与volatile的区别是：
        1. volatile的特殊规则，保证了新值能立即同步到主内存
        2. 以及每次使用前，立即从主内存刷新
    5. 因此可以说：volatile保证了多线程操作时变量的可见性，而普通变量则不能保证过这一点
* 除了volatile外，Java还有2个关键字能实现可见性：
    1. synchronized
        * 同步块的可见性是由如下规则获得的：
            ```
            对一个变量执行unlock操作之前，必须先把此变量同步回主内存中(执行store、write操作)
            ```
    2. final
        * final关键字的可见性是指：
            1. 被final修饰的字段，在构造器中一旦初始化完成
            2. 并且构造器没有把"this"的引用传递出去
                ```
                this引用逃逸是一件很危险的事情，
                其他线程有可能通过这个引用，访问到"初始化了一半"的对象
                ```
            3. 那在其他线程中，就能看见final字段的值。
##### 有序性(Ordering)：
* Java程序中天然的有序性，可以总结为一句话：
    1. 如果在本线程内观察，所有的操作都是有序的；
    2. 如果在一个线程中观察另一个线程，所有的操作都是无序的。
    ```
    前半句是指：<线程内表现为串行的语义>。
    后半句是指：指令<重排现象>和<工作内存与主内存同步延迟>现象
    ```
* Java语言提供了volatile和synchronized两个关键字，来保证线程之间操作的有序性：
    1. volatile关键字本身就包含了禁止指令重排的语义
    2. synchronized则是由如下规则获得有序性(这条规则决定了：持有同一个锁的两个同步块，只能串行地进入)
        ```
        一个变量在同一个时刻，只允许一条线程对其进行lock操作
        ```

##### "万能"的synchronized

* 可以作为以上3种特性,synchronized关键字都可以作为它的解决方案
* 大部分的并发控制操作，都可以使用synchronized来完成
* 看似"万能"的synchronized，也间接早就了它被程序员滥用的局面
* 越"万能"的并发控制，通常会伴随着越大的性能影响。
* 详见[虚拟机锁优化](../LockOptimization.md)