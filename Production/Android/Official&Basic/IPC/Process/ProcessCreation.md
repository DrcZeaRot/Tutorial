### Android进程创建流程

###### 准备知识

进程与线程的区别:

1. 进程：
    * 每个App在启动前必须先创建一个进程，该进程是由Zygote fork出来的，进程具有独立的资源空间，用于承载App上运行的各种Activity/Service等组件。
    * 进程对于上层应用来说是完全透明的，这也是google有意为之，让App程序都是运行在Android Runtime。
    * 大多数情况一个App就运行在一个进程中，除非在AndroidManifest.xml中配置Android:process属性，或通过native代码fork进程。
2. 线程：
    * 线程对应用开发者来说非常熟悉，比如每次new Thread().start()都会创建一个新的线程，该线程并没有自己独立的地址空间，而是与其所在进程之间资源共享。
    * 从Linux角度来说进程与线程都是一个task_struct结构体，除了是否共享资源外，并没有其他本质的区别。

简要介绍system_server进程和Zygote进程：

1. system_server进程：
    * 是用于管理整个Java framework层
    * 包含ActivityManager，PowerManager等各种系统服务
2. Zygote进程：
    * 是Android系统的首个Java进程，Zygote是所有Java进程的父进程
    * 包括 system_server进程以及所有的App进程都是Zygote的子进程
    * 注意这里说的是子进程，而非子线程。

##### 进程创建图示：

![进程创建图示](img/StartAppProcess.jpg)

1. App发起进程：
    * 当从桌面启动应用，则发起进程便是Launcher所在进程
    * 当从某App内启动远程进程，则发起进程便是该App所在进程
    * 发起进程先通过binder发送消息给system_server进程
2. system_server进程：
    * 调用Process.start()方法，发起创建新进程请求
    * 手机各种新进程的参数，通过socket向zygote进程发送创建新进程的请求
3. zygote进程：
    * 在执行ZygoteInit.main()后便进入runSelectLoop()循环体内
    * 当有客户端连接时便会执行ZygoteConnection.runOnce()方法，再经过层层调用后fork出新的应用进程
4. 新进程：
    * 执行handleChildProc方法，
    * 设置进程名，打开Binder驱动，启动新的Binder线程；设置虚拟机参数。
    * 最后调用ActivityThread.main()方法

#### 详解

* [system_server发起请求](ProcessCreation/SystemServerProcess.md)
* [Zygote创建进程](ProcessCreation/ZygoteProcess.md)
* [新进程的运行](ProcessCreation/NewProcess.md)

##### 参考

[理解Android进程创建流程 by Gityuan](http://gityuan.com/2016/03/26/app-process-create/)