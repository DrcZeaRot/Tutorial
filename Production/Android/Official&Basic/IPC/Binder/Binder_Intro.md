### Binder

#### 简介

Binder的多角度理解：
* 直观来说：Binder是Android的一个类，实现IBinder接口
* IPC角度：Binder是Android中的一种IPC方式
* 理解为虚拟物理设备：它的设备驱动是/dev/binder，该通信方式在Linux中没有
* Android Framework角度：Binder是ServiceManager连接
    * 各种Manager(ActivityManager、WindowManager)的相应ManagerService(AMS,WMS)的桥梁
* Android应用层：Binder是客户端和服务端，进行通信的媒介。
    * 当bindService时，服务端会返回一个包含了服务端业务调用的Binder对象
    * 通过这个Binder对象，客户端可以获取服务端提供的数据/服务
    * 这些服务包括：普通服务和基于AIDL的服务。

主要应用场景：
* 主要应用在Service中，包括AIDL和Messenger
* 普通Service中的Binder并不涉及进程间通信，较简单，不触及Binder的核心
* Messenger的底层就是AIDL。

[通过AIDL，简单了解Binder的使用、表层机制](../AIDL/AIDL.md)