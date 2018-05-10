### 新进程运行

1. ZygoteConnection::handleChildProc
    1. 关闭Zygote的Socket两端连接
    2. 设置进程名
    3. 执行目标类的main()方法 ZygoteInit.zygoteInit
2. ZygoteInit::zygoteInit
    1. RuntimeInit.redirectLogStreams：重定向log输出
    2. RuntimeInit.commonInit：通用初始化
    3. ZygoteInit.nativeZygoteInit：Zygote初始化
    4. RuntimeInit.applicationInit：应用初始化
3. RuntimeInit::commonInit
    1. 设置UncaughtExceptionHandler
    2. 设置时区
    3. 重置log配置
    4. 设置默认的HTTP User-agent格式，用于HttpURLConnection
        * 如："Dalvik/1.1.0 (Linux; U; Android 6.0.1；LenovoX3c70 Build/LMY47V)".
    5. 设置socket的tag，用于网络流量统计
4. ZygoteInit::nativeZygoteInit，JNI调用C++
    * 桥接AppRuntime::onZygoteInit(C++)
    1. ProcessState::self()：
        1. 调用open()打开dev/binder驱动设备
        2. 再利用mmap()映射内核的地址空间，将Binder驱动的fd赋值ProcessState对象中的变量mDriverFD，用于交互操作
    2. startThreadPool：创建一个新的Binder线程池，不断进行talkWithDriver
5. RuntimeInit::applicationInit
    1. 设置虚拟机内存利用率参数、targetSdkVersion
    2. 解析参数为Arguments实例
    3. 传递Arguments实例给invokeStaticMain
6. RuntimeInit::invokeStaticMain
    * 通过反射获取main方法，确保main方法的声明合理后，抛出Zygote.MethodAndArgsCaller
    * [Zygote创建进程](ZygoteProcess.md)中，Zygote::main方法会捕获这个异常
7. MethodAndArgsCaller::run
    * 注：由参数的不同，MethodAndArgsCaller虽然都是调用main方法，但也会调用不同的类的方法
    * 比如，ZygoteConnection::handleChildProc为入口的创建子进程流程中，类名为"android.app.ActivityThread"
    * 此处反射调用，跳转ActivityThread::main