### Java中的sleep与wait

![线程状态图](img/Sleep&wait.jpg)

* 两个方法分别来自Thread::sleep与Object::wait。
* 区别简述

    |方法|使用范围|锁释放|异常捕获|
    |:-:|:-:|:-:|:-:|
    |sleep|任何地方使用|不释放|必须捕获|
    |wait(配合notify/notifyAll)|3个方法都要在同步方法/同步块中调用|释放锁|无需捕获|
* 更多区别：
    1. sleep
        * 属于Thread类，表示让一个线程进入定时休眠，指定时间后自动醒来进入可运行状态
            * 并不会马上进入运行状态，线程调度机制进行恢复线程需要时间
        * 由于不会释放锁，不会出现其他线程获得锁并产生其他效果，对其他线程不会有影响。
            * sleep中的线程实例可以调用其interrupt进行打断，抛出InterruptedException
            * 如果该异常未被捕获，则线程异常终止，进入TERMINATED状态(但不捕获无法编译呀)
            * 如果捕获了该异常，可以继续执行catch/甚至finally的代码。
        * 是静态方法，只对当前线程有效；对某个线程实例调用sleep并不会让该线程sleep。
    2. wait：
        * 可以指定timeout，到时间就结束wait。
        * 是Object的实例方法，针对的是锁实例。
            * 如：Object mLock，这个实例。
                1. 它在作为某个synchronized块的锁时，调用mLock.wait()，让调用的线程释放这个锁
                2. 当另一个线程调用mLock.notify时，会有某一个由mLock作为同步锁的线程进入Runnable状态
                3. 当另一个线程调用mLock.notifyAll时，所有mLock作为同步锁的线程都进入Runnable状态
        * 如果线程拥有某个/某些对象的同步锁
            * 在调用wait之后，线程会释放它持有的所有同步资源
            * 并不限于这个调用了wait方法的对象
        * wait/notify，会对对象的锁标志([见JVM对象头]())进行操作
            * 必须在synchronized函数/synchronize块中调用
            * 如果在非同步块中调用，虽然能编译通过，但会抛出IllegalMonitorStateException的运行时异常
* 其他：
    1. 如果A线程希望立即结束B线程：
        * 可对B线程实例调用Thread::interrupt方法
            * 此时如果B线程正在wait/sleep/join，则B立刻抛出InterruptedException
            * 在catch中直接return，可以安全结束线程
        * 注意：InterruptedException是线程自己从内部抛出，而不是interrupt方法抛出的
            * 对某线程实例调用interrupt方法，该线程如果在执行普通方法，则不会抛出该异常
            * 但如果调用过interrupt，一旦线程进入wait/sleep/join状态，则立即抛出异常


### 参考

[Java中wait和sleep方法的区别](https://www.cnblogs.com/loren-Yang/p/7538482.html)