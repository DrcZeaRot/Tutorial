### Instrumentation

Instrumentation是android系统中启动Activity的一个实际操作类:
* 也就是说Activity在应用进程端的启动实际上就是Instrumentation执行的
* 实际上Activity的启动分为应用进程端的启动和SystemServer服务进程端的启动的

Instrumentation可以理解为应用进程的管家:
* ActivityThread::attach中，创建Instrumentation单例。
* Activity等实例通过ActivityThread进行attach时，接收到Instrumentation参数。
* 每一个应用程序只有一个Instrumentation对象，每个Activity内都有一个对该对象的引用。
* ActivityThread要创建或暂停某个Activity时，都需要通过Instrumentation来进行具体的操作。

代理的操作，例如：
* Instrumentation#newActivity();
* Instrumentation#newApplication();
