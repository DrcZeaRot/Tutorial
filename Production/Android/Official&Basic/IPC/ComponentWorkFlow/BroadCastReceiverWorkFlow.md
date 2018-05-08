### BroadCastReceiver工作过程

##### 静态广播

* 静态广播的注册，在应用安装时，由系统自动完成注册。
* 具体来说，是PackageManagerService完成了整个注册过程(其余的3大组件也是这样被解析、注册的)。

#### 动态广播

注册：
1. ContextWrapper::registerReceiver => ContextImpl::registerReceiver
	* 内部跳转ContextImpl::registerReceiverInternal
	* 通过LoadedApk::getReceiverDispatcher，包装Receiver为IIntentReceiver
		* 实现类是LoadedApk.ReceiverDispatcher.InnerReceiver
		* Receiver实例会被保存在ReceiverDispatcher中
	* 调用AMS::registerReceiver，传递IIntentReceiver这个Binder
2. AMS::registerReceiver
	* 整体讲，就是将远程的IBinder(InnerReceiver)和IntentFilter保存到本地

发送、接收：
1. ContextWrapper::sendBroadcast => ContextImpl::sendBroadcast
	* ContextImpl中有多个重载，但效果都基本一致,调用AMS::broadcastIntent
2. AMS::broadcastIntent => AMS::broadcastIntentLocked
	* 添加Flag：intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)，保证广播默认不会发送给已经停止的应用(3.0之后的新特性)
	* 根据IntentFilter，找出匹配的receiver，进行一系列条件过滤
	* 将符合条件的receiver，添加到BroadcastQueue队列中
	* Queue会将广播发送给相应的receiver
3. BroadcastQueue::scheduleBroadcastLocked
	* 内部调用：发消息，处理之后，跳转BroadcastQueue::processNextBroadcast
	* 已无序广播为例：遍历所有广播，将所有广播发送给匹配的receiver
	* 无序广播具体过程：deliverToRegisteredReceiverLocked => performReceiveLocked
		* 进行IPC调用：app.thread.scheduleRegisteredReceiver
4. ApplicationThread::scheduleRegisteredReceiver
	* 直接调用：InnerReceiver::performReceive => ReceiverDispatcher::performReceive
	* 内部调用Handler::post，发送ReceiverDispatcher.Args::getRunnable这个runnable
	* 在Args::getRunnable返回实例的run方法中，调用了之前保存的BroadcastReceiver::onReceiver方法

###### 动态广播的注册

从ContextWrapper::registerReceiver开始

1. 实现在ContextImpl中，最终调用到registerReceiverInternal：
    ```
    private Intent registerReceiverInternal(BroadcastReceiver receiver, int userId,
                IntentFilter filter, String broadcastPermission,
                Handler scheduler, Context context, int flags) {
        IIntentReceiver rd = null;
        if (receiver != null) {
            if (mPackageInfo != null && context != null) {
                if (scheduler == null) { scheduler = mMainThread.getHandler(); }
                rd = mPackageInfo.getReceiverDispatcher(
                    receiver, context, scheduler,
                    mMainThread.getInstrumentation(), true);
            } else {
                if (scheduler == null) { scheduler = mMainThread.getHandler(); }
                rd = new LoadedApk.ReceiverDispatcher(
                        receiver, context, scheduler, null, true).getIIntentReceiver();
            }
        }
        try {
            final Intent intent = ActivityManager.getService().registerReceiver(
                    mMainThread.getApplicationThread(), mBasePackageName, rd, filter,
                    broadcastPermission, userId, flags);
            if (intent != null) {
                intent.setExtrasClassLoader(getClassLoader());
                intent.prepareToEnterProcess();
            }
            return intent;
        } catch (RemoteException e) { ... }
    }
    ```
    * 与bindService类似，receiver与filter无法进行IPC，需要通过Binder的包装，此处是IIntentReceiver
    * LoadedApk.ReceiverDispatcher.InnerReceiver，与Service中的InnerConnection功能相同。
        * 对Binder的处理策略也类似，通过一个容器来缓存。
        ```
        public IIntentReceiver getReceiverDispatcher(BroadcastReceiver r,
                    Context context, Handler handler,
                    Instrumentation instrumentation, boolean registered) {
            synchronized (mReceivers) {
                LoadedApk.ReceiverDispatcher rd = null;
                ArrayMap<BroadcastReceiver, LoadedApk.ReceiverDispatcher> map = null;
                if (registered) {
                    map = mReceivers.get(context);
                    if (map != null) {
                        rd = map.get(r);
                    }
                }
                if (rd == null) {
                    rd = new ReceiverDispatcher(r, context, handler,
                            instrumentation, registered);
                    if (registered) {
                        if (map == null) {
                            map = new ArrayMap<BroadcastReceiver, LoadedApk.ReceiverDispatcher>();
                            mReceivers.put(context, map);
                        }
                        map.put(r, rd);
                    }
                } else {
                    rd.validate(context, handler);
                }
                rd.mForgotten = false;
                return rd.getIIntentReceiver();
            }
        }
        ```
    * 有了IBinder，将具体的注册操作，委托给AMS
2. AMS::registerReceiver
    ```
    public Intent registerReceiver(IApplicationThread caller, String callerPackage,
                IIntentReceiver receiver, IntentFilter filter, String permission, int userId,
                int flags) {
        ...
        mRegisteredReceivers.put(receiver.asBinder(), rl);

        BroadcastFilter bf = new BroadcastFilter(filter, rl, callerPackage,
                permission, callingUid, userId, instantApp, visibleToInstantApps);
        rl.add(bf);

        mReceiverResolver.addFilter(bf);
    }
    ```
    * 代码很长，只截取关键部分。
    * 整体就是将远程的IBinder(InnerReceiver)和IntentFilter对象存储起来
    * 至此，广播的注册过程结束。

###### 广播的发送、接收

ContextWrapper::sendBroadcast开始

1. ContextImpl的实现：
    ```
    public void sendBroadcast(Intent intent) {
        warnIfCallingFromSystemProcess();
        String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
        try {
            intent.prepareToLeaveProcess(this);
            ActivityManager.getService().broadcastIntent(
                    mMainThread.getApplicationThread(), intent, resolvedType, null,
                    Activity.RESULT_OK, null, null, null, AppOpsManager.OP_NONE, null, false, false,
                    getUserId());
        } catch (RemoteException e) { ... }
    }
    ```
    * sendBroadcast有多个重载，都是直接向AMS发送异步请求，用于发送广播
2. AMS::broadcastIntent => AMS::broadcastIntentLocked
    ```
    final int broadcastIntentLocked(ProcessRecord callerApp,
            String callerPackage, Intent intent, String resolvedType,
            IIntentReceiver resultTo, int resultCode, String resultData,
            Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions,
            boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
        ...
        //先添加与InstantApp相关的Flag
        if (callerInstantApp) {
            intent.setFlags(intent.getFlags() & ~Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS);
        }
        //保证广播默认不会发送给已经停止的应用(3.0之后的新特性)
        intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
        ...
    }
    ```
    * 根据intentFilter查找出匹配的receiver，并进行一系列条件过滤，符合条件的receiver被添加到BroadcastQueue队列中
    * 接着，BroadcastQueue会将广播发送给相应的receiver
        ```
        int NR = registeredReceivers != null ? registeredReceivers.size() : 0;
        if (!ordered && NR > 0) {
            // If we are not serializing this broadcast, then send the
            // registered receivers separately so they don't wait for the
            // components to be launched.
            if (isCallerSystem) {
                checkBroadcastFromSystem(intent, callerApp, callerPackage, callingUid,
                        isProtectedBroadcast, registeredReceivers);
            }
            final BroadcastQueue queue = broadcastQueueForIntent(intent);
            BroadcastRecord r = new BroadcastRecord(queue, intent, callerApp,
                    callerPackage, callingPid, callingUid, callerInstantApp, resolvedType,
                    requiredPermissions, appOp, brOptions, registeredReceivers, resultTo,
                    resultCode, resultData, resultExtras, ordered, sticky, false, userId);
            if (DEBUG_BROADCAST) Slog.v(TAG_BROADCAST, "Enqueueing parallel broadcast " + r);
            final boolean replaced = replacePending
                    && (queue.replaceParallelBroadcastLocked(r) != null);
            // Note: We assume resultTo is null for non-ordered broadcasts.
            if (!replaced) {
                queue.enqueueParallelBroadcastLocked(r);//添加到队列
                queue.scheduleBroadcastsLocked();//发送广播
            }
            registeredReceivers = null;
            NR = 0;
        }
        ```
3. BroadcastQueue::scheduleBroadcastsLocked发送广播
    * 内部调用：发消息给Handler => 匹配case => processNextBroadcast
    * BroadcastQueue::processNextBroadcast
    1. 对普通广播的处理：
        ```
        while (mParallelBroadcasts.size() > 0) {
            r = mParallelBroadcasts.remove(0);
            r.dispatchTime = SystemClock.uptimeMillis();
            r.dispatchClockTime = System.currentTimeMillis();

            if (Trace.isTagEnabled(Trace.TRACE_TAG_ACTIVITY_MANAGER)) {
                Trace.asyncTraceEnd(...);
                Trace.asyncTraceBegin(...);
            }

            final int N = r.receivers.size();
            if (DEBUG_BROADCAST_LIGHT) Slog.v(...);
            for (int i=0; i<N; i++) {
                Object target = r.receivers.get(i);
                if (DEBUG_BROADCAST)  Slog.v(...);
                deliverToRegisteredReceiverLocked(r, (BroadcastFilter)target, false, i);
            }
            addBroadcastToHistoryLocked(r);
            ...
        }
        ```
        * 无序广播存储在mParallelBroadcasts容器中
        * 遍历容器，将其中的广播，发送给所有匹配的receiver
        * 具体发送过程：deliverToRegisteredReceiverLocked => performReceiveLocked
            ```
            void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver,
                    Intent intent, int resultCode, String data, Bundle extras,
                    boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
                if (app != null) {
                    if (app.thread != null) {
                        try {
                            app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode,
                                    data, extras, ordered, sticky, sendingUser, app.repProcState);
                        } catch (RemoteException ex) {
                            synchronized (mService) {
                                Slog.w(...);
                                app.scheduleCrash("can't deliver broadcast");
                            }
                            throw ex;
                        }
                    } else {
                        throw new RemoteException("app.thread must not be null");
                    }
                } else {
                    receiver.performReceive(intent, resultCode, data, extras, ordered,
                            sticky, sendingUser);
                }
            }
            ```
        * 接收广播可以调起应用，app != null
        * 委托至ApplicationThread::scheduleRegisteredReceiver
        * 内部调用：IInnerReceiver::performReceive => ReceiverDispatcher::performReceive
            * 内部会调用Handler::post，发送ReceiverDispatcher.Args::getRunnable这个runnable
            ```
            public final Runnable getRunnable() {
                return () -> {
                    final BroadcastReceiver receiver = mReceiver;
                    try {
                        ClassLoader cl = mReceiver.getClass().getClassLoader();
                        intent.setExtrasClassLoader(cl);
                        intent.prepareToEnterProcess();
                        setExtrasClassLoader(cl);
                        receiver.setPendingResult(this);
                        receiver.onReceive(mContext, intent);
                    } catch (Exception e) { ... }
                };
            }
            ```
            * 在这个runnable内部，最终回调BroadcastReceiver::onReceive

