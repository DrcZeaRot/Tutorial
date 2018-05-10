### 点击Launcher中的图标开启应用

0. 首先，Launcher桌面也是一个Activity，它的category为：String CATEGORY_HOME = "android.intent.category.HOME"
1. 然后我们开始执行Activity启动流程
    1. 按照[Activity启动流程](../../IPC/ComponentWorkFlow/ActivityWorkFlow.md)，前进到结论的第2.3.2
    2. 此时，目标App的进程没有被创建，进入[Android进程创建流程](../../IPC/Process/ProcessCreation/SystemServerProcess.md)
2. 子进程创建成功后，进入子进程的ActivityThread::main方法，执行ActivityThread::attach(false)
    * 调用AMS::attachApplication => AMS::attachApplicationLocked
    1. IPC调用ApplicationThread::bindApplication，跳转到ActivityThread::handleBindApplication
    2. bindApplication之后，会调用ActivityStackSupervisor::attachApplicationLocked
3. ActivityThread::handleBindApplication
	1. 创建ContextImpl实例，创建Instrumentation实例并初始化
	2. 创建Application实例，注意，此时Application没有调用onCreate
	3. 启动当前进程的ContentProvider
4. ContentProvider启动完毕之后
    1. 执行Instrumentation::onCreate
    2. 执行Instrumentation::callApplicationOnCreate，调用Application::onCreate
5. Application启动完毕之后，调用ActivityStackSupervisor::attachApplicationLocked
    * 调用ActivityStackSupervisor::realStartActivityLocked
    * 至此，App启动回归Activity启动流程，继续执行到ApplicationThread::scheduleLaunchActivity