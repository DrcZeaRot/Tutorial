### Java语言中的线程安全

线程安全是如何体现的？哪些操作是线程安全的？
> 这里讨论的线程安全，就现定于多个线程之间存在共享数据访问这个前提
* 因为如果一段代码，根本不会与其他线程共享数据
* 那么从线程安全的角度来看
* 程序是串行执行还是多线程执行，对它来说是完全没有区别的。

按照线程安全的"安全程度"由强至弱来排序，Java语言中各种操作共享的数据分为：
1. 不可变
2. 绝对线程安全
3. 相对线程安全
4. 线程兼容
5. 线程对立

##### 不可变
```
Java语言中，不可变(Immutable)的对象，一定是线程安全的。
无论是对象的方法实现，来时方法的调用者，
都不需要再采取任何的线程安全保障措施。
"不可变"带来的安全性是最简单和最纯粹的。
```

* Java语言中，如果共享数据是一个基本数据类型
    * 那么只要在定义时使用final关键字修饰它，就可以保证它是不可变的
* 如果共享数据是一个对象
    * 那就需要保证对象的行为不会对其状态产生任何影响。
    * 保证对象行为不影响自己状态的途径有很多
        * 其中最简单的就是：把对象中带有状态的变量，都声明为final
        * 这样，在构造函数结束之后，它就是不可变的
##### 绝对线程安全

> 绝对的线程安全，完全满足《Java Concurrency In Practice》中的线程安全定义
* 这个定义很严格
    * 一个类要达到"不管运行时环境如何，调用者都不需要任何额外的同步措施"
    * 通常要付出很大的代价，甚至有时候是不切实际的代价
* Java API中，标注自己是线程安全的类，大都不是绝对的线程安全
    * [Vector示例，见《深入理解Java虚拟机》13.2.1节]()

##### 相对线程安全

> 相对线程安全，就是我们通常意义上所讲的"线程安全"。
* 他需要保证：
    * 对这个对象单独的操作，是线程安全的。我们再调用的时候，不需要做额外的保障措施。
    * 但对于一些特定顺序的连续调用，就可能需要在调用端使用额外的同步手段，来保证调用的正确性。
* Java API中，大部分线程安全的类都属于这种类型：
    * Vector、HashTable、Collections.synchronizedCollection()方法包装的集合等。

##### 线程兼容

* 线程兼容是指：
    * 对象本身并不是线程安全的
    * 但可以通过调用端正确地使用同步手段，来保证对象在并发环境中可以安全地使用
* 平常说一个类不是线程安全的，绝大多数时候指的是这种情况
* 如与Vector和HashTable对应的ArrayList和HashMap等

##### 线程对立

* 线程对立指：
    * 无论调用端是否采取了同步措施
    * 都无法在多线程环境中并发使用的代码。
* 由于Java语言天生就具备多线程特性，线程对立这种排斥多线程的代码是很少出现的
* 通常这种代码都是有害的，应该尽量避免。

线程对立的例子：
* 一个线程对立的例子是：Thread类的suspend()和resume()方法
* 如果有两个线程，同时持有一个线程对象。
    1. 一个尝试去中断线程，另一个尝试去恢复线程
    2. 如果并发执行的话，无论调用时是否进行了同步，目标线程都是存在死锁风险的
    3. 如果suspend()中断的线程，就是即将要执行resume()的那个线程，那就肯定要产生死锁了。
* 正式这个原因，suspend()和resume()方法已经被JDK声明废弃了。