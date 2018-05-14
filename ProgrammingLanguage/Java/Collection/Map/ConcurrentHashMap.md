### ConcurrentHashMap

#### JDK7

![JDK7中的ConcurrentHashMap结构](../img/ConcurrentHashMapInJDK7.jpg)
简介：
* 采取锁分段技术，有多个子容器，各有一把锁。并发如果发生在不同子容器上，则没有锁竞争。
* 由多个Segment组成，Segment本身是一个可重入锁。
* Segment中包含多个Node(保存键值对)，Node以链表结构存储。

常用操作：
1. get
    1. 整个get过程不需要加锁，除非读到空值才会加锁重读
    2. get方法需要的共享变量都是volatile的，保证了可见性，读取时不需要加锁。
2. put
    0. 操作共享变量时需要加锁
    1. 先通过hash值定位到Segment，先判断是否要对Segment里的链表扩容，再进行定位并插入
    2. 扩容：
        1. 判断Segment里的bin是否超过threshold，如果超过则扩容
        2. 扩容会先创建一个两倍于原容量的数组，将原数组的元素重新hash，放入新数组(只会对Segment扩容)
    3. 锁竞争场景：
        1. 线程A执行tryLock()方法成功获取锁，则把HashEntry对象插入到相应的位置；
        2. 线程B获取锁失败，则执行scanAndLockForPut()方法，在scanAndLockForPut方法中，会通过重复执行tryLock()方法尝试获取锁，在多处理器环境下，重复次数为64，单处理器重复次数为1，当执行tryLock()方法的次数超过上限时，则执行lock()方法挂起线程B；
        3. 当线程A执行完插入操作时，会通过unlock()方法释放锁，接着唤醒线程B继续执行；
3. size
    1. 先采用不加锁方式，连续计算元素个数，最多3次
    2. 如果前后两次结果相同，则认为个数准确
    3. 如果前后两次结果不同，则给每个Segment枷锁，重新计算一次。

#### JDK8

```
JDK7中的Segment设计过于臃肿，JDK8改用Node + CAS + Synchronized来保证并发安全；
并且桶中的Node的结构也是链表+树化。
```


插入操作putVal(K key, V value, boolean onlyIfAbsent)方法干的工作如下：
1. 检查key/value是否为空，如果为空，则抛异常，否则进行2
2. 进入for死循环，进行3
3. 检查table是否初始化了，如果没有，则调用initTable()进行初始化然后进行 2，否则进行4
4. 根据key的hash值计算出其应该在table中储存的位置i，取出table[i]的节点用f表示。
   * 根据f的不同有如下三种情况：
        1. 如果table[i]==null(即该位置的节点为空，没有发生碰撞)，则利用CAS操作直接存储在该位置，如果CAS操作成功则退出死循环。
        2. 如果table[i]!=null(即该位置已经有其它节点，发生碰撞)，碰撞处理也有两种情况
            1. 检查table[i]的节点的hash是否等于MOVED，如果等于，则检测到正在扩容，则帮助其扩容
            2. 说明table[i]的节点的hash值不等于MOVED，如果table[i]为链表节点，则将此节点插入链表中即可
        3. 如果table[i]为树节点，则将此节点插入树中即可。插入成功后，进行 5
5. 如果table[i]的节点是链表节点，则检查table的第i个位置的链表是否需要转化为数，如果需要则调用treeifyBin函数进行转化