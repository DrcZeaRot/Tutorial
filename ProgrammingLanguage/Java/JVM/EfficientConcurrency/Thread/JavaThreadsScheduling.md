### Java线程调度(ThreadsScheduling)

* 线程调度是指：系统为线程分配处理器使用权的过程。
* 主要调度方式有两种：
    1. 协同式线程调度(Cooperative Threads-Scheduling)
    2. 抢占式线程调度(Preemptive Threads-Scheduling)

##### 使用协同式调度

* 线程的执行时间，由线程本身来控制
* 线程把自己的工作执行完了之后，主动通知系统，切换到另外一个线程上。
* 最大的好处：实现简单
    * 而且由于线程要把自己的事情干完后才会进行线程切换
    * 切换操作对线程自己是可知的，所以没有什么线程同步的问题
* Lua语言中的"协同例程"就是这类实现。
* 坏处很明显：
    1. 线程执行时间不可控制
    2. 甚至如果一个线程编写有问题，一直不告诉系统进行线程切换
    3. 程序就会一直阻塞在那里。
* 一个进程坚持不让出CPU执行时间，就可能会导致整个系统崩溃。

##### 使用抢占式调度

* 每个线程将由系统来分配执行时间
* 线程的切换不由线程本身来决定
    ```
    Java中，Thread.yeild()可以让出执行时间。
    但是要获取执行时间的话，线程本身是没有什么办法的
    ```
* 这种实现下：
    1. 线程的执行时间是系统可控的
    2. 也不会有一个线程导致整个进程阻塞的问题
* 目前，Java使用的线程调度方式，就是抢占式调度(官方Coroutine貌似没戏了？)

##### 线程优先级

优先级的引入：
* 虽然Java线程调度是系统自动完成的
* 但是我们还是可以"建议"系统给某些线程多分配一点执行时间、另外一些线程则可以少分配一点
* 这项操作可以通过设置线程优先级来完成
* Java语言一共设置了10个级别的线程优先级(Thread.Min_PRIORITY至Thread.MAX_PRIORITY)
* 两个线程同时处于Ready状态时，优先级越高的线程，越容易被系统选择执行。

但优先级并不靠谱：
* 原因是：Java的线程，是通过映射到系统的原生线程上，来实现的
* 所以，线程调度最终，还是取决于操作系统
* 虽然现在很多操作系统都提供线程优先级的概念，但并不见得能与Java线程的优先级一一对应。
* 所谓不靠谱，并不仅仅是说"在一些平台上，不同优先级实际会变得相同"这一点
    * 还有其他情况，让我们不能太依赖优先级：
    * 优先级可能会被系统自行改变
* 因此，我们不能再程序中，通过优先级来完全准确地判断一组状态都为Ready的线程，将会先执行哪一个。
