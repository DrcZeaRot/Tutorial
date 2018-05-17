## Handler
### 消息机制主要包含：
[消息机制架构图](..\img\AndroidMessageFramework.jpg)
###### Java层
* Message：消息分为硬件产生的消息(如按钮、触摸)和软件生成的消息；
* MessageQueue：消息队列的主要功能向消息池投递消息(MessageQueue.enqueueMessage)和取走消息池的消息(MessageQueue.next)；
* Handler：消息辅助类，主要功能向消息池发送各种消息事件(Handler.sendMessage)和处理相应消息事件(Handler.handleMessage)；
* Looper：不断循环执行(Looper.loop)，按分发机制将消息分发给目标处理者。
###### C++层
* NativeMessageQueue.cpp
* Looper.cpp

[消息机制流程图](..\img\AndroidMessageFlow.jpg)
>消息队列没有消息时进入空闲等待，有消息时才获取并发送消息。这种机制是通过[pipe(管道)](../../IPC/Pipe.md)机制实现的
##### 一切从ActivityThread.main()开始
>当我们启动一个APK时，ActivityManagerService会为我们的Activity创建并启动一个主线程(ActivityThread对象)


### 消息循环准备阶段
1. ActivityThread.main()
    * 创建了一个ActivityThread对象
    * Looper.prepareMainLooper()创建主线程Looper，Looper.loop()进入消息循环
2. Looper.prepareMainLooper()
    * 调用Looper.prepare(false)创建一个新的Looper对象，此Looper的mMessageQueue无法退出
    * 将新建的Looper对象存放在Looper的sThreadLocal成员中，此成员为[ThreadLocal类型](../../../../../ProgrammingLanguage/Java/Concurrency/ThreadLocal.md)
    * ThreadLocal保证调用Looper.prepare()的线程中都会有一个独立的Looper对象
    * Looper构造函数中创建自己的消息队列MessageQueue
3. MessageQueue实例化
    * 构造方法调用JNI nativeInit(), 此native方法会创建一个NativeMessageQueue对象
4. NativeMessageQueue实例化
    * 实例化时，创建一个Looper(C++)对象指向mLooper，调用Looper::setForThread(mLooper)
5. Looper(C++)实例化(见上述Pipe管道)
>至此，就介绍完消息循环的创建/准备阶段。该结点的主要工作可以概括为2部分内容：
* Java层，创建Looper对象，Looper的构造函数中会创建消息队列MessageQueue的对象。MessageQueue的作用存储消息队列，用来管理消息的。
* C++层，消息队列创建时，会调用JNI函数，初始化NativeMessageQueue对象。NativeMessageQueue则会初始化Looper对象。Looper的作用就是，当Java层的消息队列中没有消息时，就使Android应用程序主线程进入等待状态，而当Java层的消息队列中来了新的消息后，就唤醒Android应用程序的主线程来处理这个消息。
### 消息循环
1. Looper.loop()
    * 尝试从mQueue获取下一条消息 queue.next()
    * queue.next()方法可能阻塞，阻塞则一直卡在这个无线循环中
    * 如果下一条消息为空，退出loop循环、否则msg.target.dispatchMessage(msg)分发消息
2. MessageQueue.next()
    * 进行一些简单的参数初始化之后，就进入无限循环
    * 循环中调用nativePollOnce()进入C++层，此方法接收的参数中，有一个是<下一次需要唤醒的时机>(nextPollTimeoutMillis)
        * 这个nextPollTimeoutMillis第一次进入循环时是0
        1. 一路压栈 来到Looper::pollInner()，通过epoll_await()，开始等待Pipe的IO事件
        2. 如果Pipe上有IO事件发生(有消息待处理)，调用Looper::awoken()取出消息。
        3. awoken()只是将Pipe中的内容都读取出来，通过Pipe的读写来改变主线程的等待/唤醒状态
    * 关于msg的定时问题：
        1. 所有消息在被发送给消息队列之后，按照绝对时间顺序排好
        2. 假如：最新的一条消息被读到之后，发现目前还不需要执行(假设要在60s之后执行)
            1. 可以肯定的是：如果没有新的消息被发送，则未来60s不需要处理消息
            2. 此时，由于next是在Looper::loop的无限循环中，
                * 会记录最近一次消息的时间nextPollTimeoutMillis
                * 将mBlock成员设置为true
                * 并continue，继续循环
            3. 此时会再调用nativePollOnce(ptr,nextPollTimeoutMillis),也就是在这段间隔之后再尝试唤醒
            4. 如果没有新的消息，则线程会进入空闲等待状态
            5. 如果没等到nextPollTimeoutMillis这段时间，有新的消息发送了，则唤醒线程，继续处理消息。
>至此，"消息循环"部分就介绍完毕了！
### 消息发送
1. ActivityThread.ApplicationThread.scheduleLaunchActivity()
    * 此处涉及[startActivity()流程](../Launcher/startActivity.md)
    * 调用ActivityThread.sendMessage() ==> 创建一个Message并通过mH.sendMessage(msg)发送
2. Handler
    * Handler中包含多种发送消息的方法，但最终在同一个enqueueMessage()方法汇总
    * sendMessage()/post() => sendMessageDelayed() => sendMessageAtTime() => enqueueMessage()
    * enqueueMessage()调用queue.enqueueMessage()将Message添加至消息队列
3. MessageQueue.enqueueMessage()
    1. 将Message按照绝对时间顺序插入到合适位置
    2. 消息队列为空：插入只考虑新的一条消息，此时主线程为空闲等待状态，调用nativeWake()唤醒它
    3. 消息队列不为空：插入要对整个队列进行移动
        * 还会判断mBlock以及一些相关的flag，来确定needWake值
        * 如果在新消息发送之前，MessageQueue判断目前一段时间没有消息要处理<因为Message可能是要在未来才需要处理>，就会将mBlock设置为true，并让线程进入空闲等待状态
        * 如果needWake，就调用nativeWake()唤醒线程
4. nativeWake
    * nativeMessageQueue->wake() ==> mLooper->wake()
    * Looper::wake()中，向Pipe中写入写入一个"W"，作为唤醒信号
    * 对应之前的Looper::awoken()，它也只会读取到一个"W"，作为信号
>前面我们在分析应用程序的消息循环时说到，当应用程序的消息队列中没有消息处理时，应用程序的主线程就会进入空闲等待状态，而这个空闲等待状态就是通过调用这个Looper类的pollInner()函数来进入的，具体就是在pollInner()函数中调用epoll_wait()函数来等待管道中有内容可读的。这样，就将发送消息和接收消息联系起来了。
### 消息的处理
1. Looper.loop()
    * 再回到消息开始循环的起始处：msg.target.dispatchMessage(msg)
    * Handler.enqueueMessage()中，通过msg.target = this 给Message添加一个Handler的引用
2. Handler.dispatchMessage() 共3个分支
    * post(Runnable)模式发送的消息，通过msg.callback = runnable，封装runnable到message中
    * Handler(Handler.Callback)构造方法，封装callback到Handler.mCallback中
    1. msg.callback != null ==> 直接msg.callback.run()
    2. mCallback != null ==> 优先处理mCallBack.handleMessage(msg)
    3. mCallBack.handleMessage(msg) == true  ==> 处理handleMessage
### 总体流程简单梳理
* 应用程序先通过Looper.prepareMainLooper()来创建消息队列。
在创建消息队列的过程中，会创建Looper对象，MessageQueue对象，并调用JNI函数；
Looper.loop()无限循环，尝试从MessageQueue中获取消息，没有新消息则调用JNI，通过管道来进入空闲等待状态。
* 当应用程序调用sendMessage()或其他类似接口发送消息时，消息会被添加到消息队列；
若需要唤醒，则调用JNI函数，唤醒管道上处于空闲状态的主线程。
* 管道上的空闲状态的主线程被唤醒之后，就会读出消息队列的消息，然后通过dispatchMessage()来分发处理。
最终，消息在dispatchMessage中以3中分支被不同处理。

[Handler常见使用](HandlerSample.md)

#### 参考
* [Android消息机制架构和源码解析 by wangkuiwu](http://wangkuiwu.github.io/2014/08/26/MessageQueue/)
* [Android消息机制，从Java层到Native层剖析 by 溜了溜了](https://zhuanlan.zhihu.com/p/29929031)
* [Android消息机制 by gityuan](http://gityuan.com/2015/12/26/handler-message-framework/)