### Service工作过程

#### 从结论说

startService：
1. ContextWrapper::startService => ContextImpl::startService
	* 内部跳转 ContextImpl::startServiceCommon => AMS::startService
2. AMS::startService
	* 桥接：ActiveServices::startServiceLocked
	* 多次内部跳转，到达ActiveServices::realStartServiceLocked
	* 进行IPC调用：app.thread.scheduleCreateService
	* 之后会调用sendServiceArgsLocked => 进行IPC调用r.app.thread.scheduleServiceArgs(r, slice);
3. ApplicationThread::scheduleCreateService
	* 发消息、处理之后，跳转ActivityThread::handleCreateService
4. ActivityThread::handleCreateService
	1. 通过LoadedApk.getClassLoader的类加载器，创建Service实例
	2. 创建ContextImpl实例、获取Application实例
	3. 调用Service::attach完成初始化
	4. 然后直接调用Service::onCreate
	5. 并将Service放入mService容器中。
5. ApplicationThread::scheduleServiceArgs => ActivityThread::handleServiceArgs
    * 调用Service::onStartCommand

bindService：
1. ContextWrapper::bindService => ContextImpl::bindService
	* 内部跳转ContextImpl::bindServiceCommon
	* 通过LoadedApk::getServiceDispatcher，包装ServiceConnection为IServiceConnection
		* 实现类是：LoadedApk.ServiceDispatcher.InnerConnection
		* ServiceConnection会被保存在ServiceDispatcher中
	* 调用AMS::bindService，传递IServiceConnection这个Binder
2. AMS::bindService
	* 桥接：ActiveServices::bindServiceLocked
	* 多次内部跳转，过程中调用ActiveServices::realStartServiceLocked，进行Service的创建于onCreate回调
	* 还会调用ActiveServices::requestServiceBindingLocked，进行Service的绑定
	* 进行IPC调用：app.thread.scheduleBindService
3. ApplicationThread::scheduleBindService
	* 发消息、处理之后，跳转ActivityThread::handleBindService
4. ActivityThread::handleBindService
	1. 从Service容器mServices中，通过IBinder的token获得待bind的Service实例(在startService过程中添加到容器里)
	2. 如果第一次绑定，直接调用Service::onBind方法
	3. 并接下来通过AMS::publishService，通知ServiceConnection::onServiceConnection
5. AMS::publishService
	* 桥接：ActiveServices::publishServiceLocked
	* 调用IServiceConnection::connected方法
	* 实际是调用LoadedApk.ServiceDispatcher.InnerConnection::connected
	* 跳转调用ServiceDispatcher::connected => ServiceDispatcher::doConnected
	* 内部调用之前保存的ServiceConnection::onServiceConnection

#### Start Service

从ContextWrapper::startService到IApplication::scheduleCreateService：

0. 入口方法ContextWrapper::startService：
    ```
    public ComponentName startService(Intent service) {
        return mBase.startService(service);
    }
    ```
    * mBase在Activity::attach时赋值，是一个ContextImpl实例。
1. ContextImpl::startService：
    ```
    public ComponentName startService(Intent service) {
        warnIfCallingFromSystemProcess();
        return startServiceCommon(service, false, mUser);
    }
    private ComponentName startServiceCommon(Intent service, boolean requireForeground,
            UserHandle user) {
        try {
            validateServiceIntent(service);
            service.prepareToLeaveProcess(this);
            ComponentName cn = ActivityManager.getService().startService(
                mMainThread.getApplicationThread(), service, service.resolveTypeIfNeeded(
                            getContentResolver()), requireForeground,
                            getOpPackageName(), user.getIdentifier());
            if (cn != null) {
                if (cn.getPackageName().equals("!")) {
                    throw new SecurityException(...);
                } else if (cn.getPackageName().equals("!!")) {
                    throw new SecurityException(...);
                } else if (cn.getPackageName().equals("?")) {
                    throw new IllegalStateException(...);
                }
            }
            return cn;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    ```
    * 似曾相识的桥段：ActivityManager.getService().startService(..)
    * 此处，前往AMS::startService
2. AMS::startService：
    ```
    public ComponentName startService(IApplicationThread caller, Intent service,
            String resolvedType, boolean requireForeground, String callingPackage, int userId)
            throws TransactionTooLargeException {
        enforceNotIsolatedCaller("startService");
        // Refuse possible leaked file descriptors
        if (service != null && service.hasFileDescriptors() == true) { ... }
        if (callingPackage == null) { ... }

        if (DEBUG_SERVICE) Slog.v(...);
        synchronized(this) {
            final int callingPid = Binder.getCallingPid();
            final int callingUid = Binder.getCallingUid();
            final long origId = Binder.clearCallingIdentity();
            ComponentName res;
            try {
                res = mServices.startServiceLocked(caller, service,
                        resolvedType, callingPid, callingUid,
                        requireForeground, callingPackage, userId);
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
            return res;
        }
    }
    ```
    * mService类型为：ActiveServices，是一个辅助AMS进行Service管理的类，包括Service的启动、绑定、停止等。
    * 桥接至ActiveServices::startServiceLocked => startServiceInnerLocked
3. ActiveServices::startServiceInnerLocked：
    ```
    ComponentName startServiceInnerLocked(ServiceMap smap, Intent service, ServiceRecord r,
            boolean callerFg, boolean addToStarting) throws TransactionTooLargeException {
        ServiceState stracker = r.getTracker();
        ...
        String error = bringUpServiceLocked(r, service.getFlags(), callerFg, false, false);
        ...
        return r.name;
    }
    ```
    * ServiceRecord类似ActivityRecord，描述一个Service记录，贯穿整个启动流程。
    * 内部调用：bringUpServiceLocked => realStartServiceLocked
4. ActiveServices::realStartServiceLocked：
    ```
    app.thread.scheduleCreateService(r, r.serviceInfo,
            mAm.compatibilityInfoForPackageLocked(r.serviceInfo.applicationInfo),
            app.repProcState);
    ...
    sendServiceArgsLocked(r, execInFg, true);
    ```
    * 方法名到realStartXXXLocked时，就要轮到IApplicationThread进行IPC了。
    * sendServiceArgsLocked通过IPC回调其他Service的方法，如onStartCommand
        ```
        r.app.thread.scheduleServiceArgs(r, slice);
        ```


IApplicationThread对scheduleCreateService的实现：
1. ApplicationThread::scheduleCreateService => ActivityThread::handleCreateService
    * ActivityThread::handleCreateService：
        1. 创建Service实例：
            ```
            LoadedApk packageInfo = getPackageInfoNoCheck(...);
            Service service = null;
            try {
                java.lang.ClassLoader cl = packageInfo.getClassLoader();
                service = (Service) cl.loadClass(data.info.name).newInstance();
            } catch (Exception e) { ... }
            ```
        2. 创建ContextImpl实例、获取Application实例，并调用service.attach，完成service初始化。
            ```
            try {
                ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
                context.setOuterContext(service);

                Application app = packageInfo.makeApplication(false, mInstrumentation);
                service.attach(context, this, data.info.name, data.token, app,
                        ActivityManager.getService());
                service.onCreate();
                mServices.put(data.token, service);
                try {
                    ActivityManager.getService().serviceDoneExecuting(
                            data.token, SERVICE_DONE_EXECUTING_ANON, 0, 0);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            ```
            * attach之后，会直接回调Service::onCreate
            * 将service放进mServices容器中
                ```
                final ArrayMap<IBinder, Service> mServices = new ArrayMap<>();
                ```
2. ApplicationThread::scheduleServiceArgs => ActivityThread::handleServiceArgs
    * ActivityThread::handleServiceArgs
    ```
    private void handleServiceArgs(ServiceArgsData data) {
        Service s = mServices.get(data.token);
        if (s != null) {
            try {
                ...
                if (!data.taskRemoved) {
                    res = s.onStartCommand(data.args, data.flags, data.startId);
                } else { ... }

                QueuedWork.waitToFinish();

                try {
                    ActivityManager.getService().serviceDoneExecuting(...);
                } catch (RemoteException e) { ... }
                ensureJitEnabled();
            } catch (Exception e) { ... }
        }
    }
    ```

#### Bind Service

从ContextWrapper::bindService到IApplication::scheduleBindService：

1. 与startService类似，查看ContextImpl的bindService实现：
    ```
    public boolean bindService(Intent service, ServiceConnection conn,
                int flags) {
        warnIfCallingFromSystemProcess();
        return bindServiceCommon(service, conn, flags, mMainThread.getHandler(),
                Process.myUserHandle());
    }

    private boolean bindServiceCommon(Intent service, ServiceConnection conn, int flags, Handler
                handler, UserHandle user) {
        // Keep this in sync with DevicePolicyManager.bindDeviceAdminServiceAsUser.
        IServiceConnection sd;
        if (conn == null) { ... }
        if (mPackageInfo != null) {
            sd = mPackageInfo.getServiceDispatcher(conn, getOuterContext(), handler, flags);
        } else { ... }
        validateServiceIntent(service);
        try {
            IBinder token = getActivityToken();
            ...
            service.prepareToLeaveProcess(this);
            int res = ActivityManager.getService().bindService(
                mMainThread.getApplicationThread(), getActivityToken(), service,
                service.resolveTypeIfNeeded(getContentResolver()),
                sd, flags, getOpPackageName(), user.getIdentifier());
            if (res < 0) { ... }
            return res != 0;
        } catch (RemoteException e) { ... }
    }
    ```
    * 将客户端的ServiceConnection实例，转化为ServiceDispatcher.InnerConnection实例。
        * 服务的绑定可能会触发IPC，必须借助Binder才能实现通信。
        * ServiceDispatcher.InnerConnection恰好可以充当这个Binder。
            ```
            LoadedApk.ServiceDispatcher.InnerConnection：
            private static class InnerConnection extends IServiceConnection.Stub
            ```
    * 上述转化过程通过LoadedApk::getServiceDispatcher完成
        ```
        //存储当前活动的ServiceConnection与Dispatcher的映射。
        private final ArrayMap<Context, ArrayMap<ServiceConnection, LoadedApk.ServiceDispatcher>> mServices = new ArrayMap<>();

        public final IServiceConnection getServiceDispatcher(ServiceConnection c,
                    Context context, Handler handler, int flags) {
            synchronized (mServices) {
                LoadedApk.ServiceDispatcher sd = null;
                ArrayMap<ServiceConnection, LoadedApk.ServiceDispatcher> map = mServices.get(context);
                if (map != null) {
                    if (DEBUG) Slog.d(TAG, "Returning existing dispatcher " + sd + " for conn " + c);
                    sd = map.get(c);
                }
                if (sd == null) {
                    sd = new ServiceDispatcher(c, context, handler, flags);
                    if (DEBUG) Slog.d(TAG, "Creating new dispatcher " + sd + " for conn " + c);
                    if (map == null) {
                        map = new ArrayMap<>();
                        mServices.put(context, map);
                    }
                    map.put(c, sd);
                } else {
                    sd.validate(context, handler);
                }
                return sd.getIServiceConnection();
            }
        }
        ```
        * Dispatcher中保存有ServiceConnection和InnerConnection的引用。
        * Service与客户端连接后，系统会通过InnerConnection调用ServiceConnection::onServiceConnected，可能会触发IPC。
        * 此方法尝试获取容器中已存在的IServiceConnection(实现是InnerConnection)，找不到就创建新的并放进容器里。
        * 最后返回对应的InnerConnection
    * Dispatcher创建完毕之后，通过AMS::bindService进行后续过程。
2. AMS::bindService：
    ```
    public int bindService(IApplicationThread caller, IBinder token, Intent service,
                String resolvedType, IServiceConnection connection, int flags, String callingPackage,
                int userId) throws TransactionTooLargeException {
        enforceNotIsolatedCaller("bindService");
        if (service != null && service.hasFileDescriptors() == true) { ... }
        if (callingPackage == null) { ... }

        synchronized(this) {
            return mServices.bindServiceLocked(caller, token, service,
                    resolvedType, connection, flags, callingPackage, userId);
        }
    }
    ```
    * 跳转ActiveServices::bindServiceLocked，内部继续调用：
        1. bringUpServiceLocked => realStartServiceLocked
            * 此处执行与与startService类似，进行Service的创建与onCreate回调。
        2. 还会调用ActiveServices::requestServiceBindingLocked，进行Service的绑定
3. ActiveServices::requestServiceBindingLocked：
    ```
    private final boolean requestServiceBindingLocked(ServiceRecord r, IntentBindRecord i,
            boolean execInFg, boolean rebind) throws TransactionTooLargeException {
        if (r.app == null || r.app.thread == null) { return false; }
        if (DEBUG_SERVICE) Slog.d(...);
        if ((!i.requested || rebind) && i.apps.size() > 0) {
            try {
                bumpServiceExecutingLocked(r, execInFg, "bind");
                r.app.forceProcessStateUpTo(ActivityManager.PROCESS_STATE_SERVICE);
                r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind,
                        r.app.repProcState);
                if (!rebind) {
                    i.requested = true;
                }
                i.hasBound = true;
                i.doRebind = false;
            } catch (TransactionTooLargeException e) { ... }
            catch (RemoteException e) { ... }
        }
        return true;
    }
    ```
    * 跳转ApplicationThread::scheduleBindService

ApplicationThread对scheduleBindService的实现：

1. ApplicationThread::scheduleBindService => ActivityThread::handleBindService：
    ```
    private void handleBindService(BindServiceData data) {
        Service s = mServices.get(data.token);
        ...
        if (s != null) {
            try {
                data.intent.setExtrasClassLoader(s.getClassLoader());
                data.intent.prepareToEnterProcess();
                try {
                    if (!data.rebind) {
                        IBinder binder = s.onBind(data.intent);
                        ActivityManager.getService().publishService(
                                data.token, data.intent, binder);
                    } else {
                        s.onRebind(data.intent);
                        ActivityManager.getService().serviceDoneExecuting(
                                data.token, SERVICE_DONE_EXECUTING_ANON, 0, 0);
                    }
                    ensureJitEnabled();
                } catch (RemoteException ex) {
                    throw ex.rethrowFromSystemServer();
                }
            } catch (Exception e) {
                if (!mInstrumentation.onException(s, e)) {
                    throw new RuntimeException(
                            "Unable to bind to service " + s
                            + " with " + data.intent + ": " + e.toString(), e);
                }
            }
        }
    }
    ```
    1. 从Service容器中，通过IBinder的token获取需要bind的Service实例
    2. 调用Service::onBind方法，获取返回的IBinder实例。
        * onBind方法只会执行一次，多次绑定会执行onRebind
    3. 通过AMS::publishService，通知ServiceConnection的onServiceConnection。
2. AMS::publishService => ActiveServices::publishServiceLocked
    ```
    void publishServiceLocked(ServiceRecord r, Intent intent, IBinder service) {
            final long origId = Binder.clearCallingIdentity();
            try {
                ...
                if (r != null) {
                    Intent.FilterComparison filter = new Intent.FilterComparison(intent);
                    IntentBindRecord b = r.bindings.get(filter);
                    if (b != null && !b.received) {
                        b.binder = service;
                        b.requested = true;
                        b.received = true;
                        for (int conni=r.connections.size()-1; conni>=0; conni--) {
                            ArrayList<ConnectionRecord> clist = r.connections.valueAt(conni);
                            for (int i=0; i<clist.size(); i++) {
                                ConnectionRecord c = clist.get(i);
                                if (!filter.equals(c.binding.intent.intent)) {
                                    ...
                                    continue;
                                }
                                ...
                                try {
                                    c.conn.connected(r.name, service, false);
                                } catch (Exception e) { ... }
                            }
                        }
                    }

                    serviceDoneExecutingLocked(r, mDestroyingServices.contains(r), false);
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    ```
    * 调用InnerConnection::connected方法 => ServiceDispatcher::connected
        ```
        public void ServiceDispatcher.connected(ComponentName name, IBinder service, boolean dead) {
            if (mActivityThread != null) {
                mActivityThread.post(new RunConnection(name, service, 0, dead));
            } else {
                doConnected(name, service, dead);
            }
        }

        public void RunConnection.run() {
            if (mCommand == 0) {
                doConnected(mName, mService, mDead);
            } else if (mCommand == 1) {
                doDeath(mName, mService);
            }
        }
        ```
    * ServiceDispatcher::doConnected：ServiceDispatcher也是IServiceConnection.Stub。
        ```
        public void doConnected(ComponentName name, IBinder service, boolean dead) {
            ServiceDispatcher.ConnectionInfo old;
            ServiceDispatcher.ConnectionInfo info;

            synchronized (this) {
                if (mForgotten) {
                    // We unbound before receiving the connection; ignore
                    // any connection received.
                    return;
                }
                old = mActiveConnections.get(name);
                if (old != null && old.binder == service) {
                    return;
                }

                if (service != null) {
                    // A new service is being connected... set it all up.
                    info = new ConnectionInfo();
                    info.binder = service;
                    info.deathMonitor = new DeathMonitor(name, service);
                    try {
                        service.linkToDeath(info.deathMonitor, 0);
                        mActiveConnections.put(name, info);
                    } catch (RemoteException e) {
                        mActiveConnections.remove(name);
                        return;
                    }
                } else {
                    mActiveConnections.remove(name);
                }
                if (old != null) {
                    old.binder.unlinkToDeath(old.deathMonitor, 0);
                }
            }

            if (old != null) {
                mConnection.onServiceDisconnected(name);
            }
            if (dead) {
                mConnection.onBindingDied(name);
            }
            // If there is a new service, it is now connected.
            if (service != null) {
                mConnection.onServiceConnected(name, service);
            }
        }
        ```
        * 由于构建ServiceDispatcher时，已经将ServiceConnection的引用保存，此处直接调用ServiceConnection::onServiceConnected