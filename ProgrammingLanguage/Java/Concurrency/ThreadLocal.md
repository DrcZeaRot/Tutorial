## ThreadLocal

##### 前言
1. 什么是ThreadLocal？
    >ThreadLocal类顾名思义可以理解为线程局部变量(LocalVariable)。也就是说如果定义了一个ThreadLocal，每个线程往这个ThreadLocal中读写是线程隔离，互相之间不会影响的。它提供了一种将可变数据通过每个线程有自己的独立副本从而实现线程封闭的机制。
2. 它大致的实现思路是怎样的？
    >Thread类有一个类型为ThreadLocal.ThreadLocalMap的实例变量threadLocals，也就是说每个线程有一个自己的ThreadLocalMap。ThreadLocalMap有自己的独立实现，可以简单地将它的key视作ThreadLocal，value为代码中放入的值（实际上key并不是ThreadLocal本身，而是它的一个弱引用）。每个线程在往某个ThreadLocal里塞值的时候，都会往自己的ThreadLocalMap里存，读也是以某个ThreadLocal作为引用，在自己的map里找对应的key，从而实现了线程隔离。
##### ThreadLocal与Synchronized的区别
* ThreadLocal和Synchronized都用于解决多线程并发访问。但是ThreadLocal与synchronized有本质的区别。
* synchronized是利用锁的机制，使变量或代码块在某一时该只能被一个线程访问。而ThreadLocal为每一个线程都提供了变量的副本，使得每个线程在某一时间访问到的并不是同一个对象，这样就隔离了多个线程对数据的数据共享。而Synchronized却正好相反，它用于在多个线程间通信时能够获得数据共享。
* Synchronized用于线程间的数据共享，而ThreadLocal则用于线程间的数据隔离。
##### ThreadLocal建议
* ThreadLocal应定义为静态成员变量。
* 能通过传值传递的参数，不要通过ThreadLocal存储，以免造成ThreadLocal的滥用。
* 在线程池的情况下，线程结束后，对象不会被销毁，ThreadLocal的value就不会被回收。所以在ThreadLocal业务周期处理完成时，最好显式的调用remove()方法，清空”线程局部变量”中的值。
* 正常情况下使用ThreadLocal不会造成内存溢出，弱引用的只是threadLocal，保存的值依然是强引用的，如果threadLocal依然被其他对象强引用，”线程局部变量”是无法回收的。
#### 参考
* [ThreadLocal源码解读 by 活在梦里](https://www.cnblogs.com/micrari/p/6790229.html)
* [深入JDK源码之ThreadLocal类](https://my.oschina.net/xianggao/blog/392440?fromerr=CLZtT4xC)
* [【Java 并发】详解 ThreadLocal](https://www.cnblogs.com/zhangjk1993/archive/2017/03/29/6641745.html)