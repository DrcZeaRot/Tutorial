### Activity工作过程

context::startActivity/startActivityForResult方法，究竟发生了什么：

```
startActivity(Intent(context,XxxActivity::class.java))
```

#### 从结论说

1. Activity::startActivity => Instrumentation::execStartActivity => AMS::startActivity
2. 内部多次跳转，到达： AMS::startActivityAsUser
    1. 第一次桥接：跳转ActivityStarter::startActivityMayWait
        * 内部多次跳转，到达ActivityStarter::startActivityUnchecked
    2. 第二次桥接：跳转ActivityStackSupervisor::resumeFocusedStackTopActivityLocked
    3. 第三次桥接：ActivityStack::resumeTopActivityUncheckedLocked
        * 内部跳转，到达ActivityStack::resumeTopActivityInnerLocked
        * 此处先尝试pause当前Activity
        * 再尝试resume目标Activity，如果无法resume，需要重新启动目标Activity
    3. 反向桥接(如果需要重新启动)：ActivityStackSupervisor::startSpecificActivityLocked
        * 内部跳转：ActivityStackSupervisor::realStartActivityLocked
    4. 达到realXxxLocked时，就是真正的IPC的时刻：app.thread.scheduleLaunchActivity
3. ApplicationThread::scheduleLaunchActivity
    * 发消息给Handler mH => handleMessage进行case分发 => ActivityThread::handleLaunchActivity
4. ActivityThread::handleLaunchActivity
    1. performLaunchActivity创建Activity实例：
    	1. 从ActivityClientRecord获取组件信息
    	2. 创建ContextImpl
    	3. 通过ContextImpl::getClassLoader的类加载器，创建Activity实例
    	4. 获取Application实例
    	5. 回调Activity::attach，完成初始化
    	    * Activity::attach，调用ContextWrapper::attachBaseContext，将ContextImpl赋值给mBase。
    	6. Instrumentation::callActivityOnCreate回调Activity::onCreate
    	7. Activity::performStart回调 => Instrumentation::callActivityOnStart(this) => Activity::onStart
    2. performResumeActivity回调Activity的onResume
    	* Activity::performResume => Instrumentation::callActivityOnResume(this) => Activity::onResume
5. 至此，Activity已经进入Resume状态

##### 从startActivity到IApplicationThread::scheduleLaunchActivity：

0. 入口方法：
    ```
    public void startActivity(Intent intent, @Nullable Bundle options) {
        if (options != null) {
            startActivityForResult(intent, -1, options);
        } else {
            // Note we want to go through this call for compatibility with
            // applications that may have overridden the method.
            startActivityForResult(intent, -1);
        }
    }
    ```
1. 多个重载，最终汇入同一个方法：
    ```
     public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
            @Nullable Bundle options) {
        if (mParent == null) {
            options = transferSpringboardActivityOptions(options);
            Instrumentation.ActivityResult ar =
                mInstrumentation.execStartActivity(
                    this, mMainThread.getApplicationThread(), mToken, this,
                    intent, requestCode, options);
            if (ar != null) {
                mMainThread.sendActivityResult(
                    mToken, mEmbeddedID, requestCode, ar.getResultCode(),
                    ar.getResultData());
            }
            if (requestCode >= 0) {
                mStartedActivity = true;
            }
            cancelInputsAndStartExitTransition(options);
        } else {
            ...
        }
    }
    ```
    * 只需要分析mParent == null的分支。
        > mParent代表ActivityGroup(Api13时废弃)，原用作在一个界面中嵌入多个子Activity，目前使用Fragment实现类似需求。
    * 执行Instrumentation::execStartActivity
        * [关于Instrumentation](Instrumentation.md)
        * 注意：
            1. mMainThread是ActivityThread实例。
            2. mMainThread.getApplicationThread()，是一个ApplicationThread实例。
    * 如果execStartActivity有返回ActivityResult，再通过ActivityThread::ActivityResult处理result。
2. Instrumentation::execStartActivity
    ```
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, String target,
            Intent intent, int requestCode, Bundle options) {
            IApplicationThread whoThread = (IApplicationThread) contextThread;
        if (mActivityMonitors != null) { ... }
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            int result = ActivityManager.getService()
                .startActivity(whoThread, who.getBasePackageName(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        token, target, requestCode, 0, null, options);
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
        return null;
    }
    ```
    * ActivityManager::getService：
        ```
        public static IActivityManager getService() {
            return IActivityManagerSingleton.get();
        }

        private static final Singleton<IActivityManager> IActivityManagerSingleton =
                new Singleton<IActivityManager>() {
                    @Override
                    protected IActivityManager create() {
                        final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                        final IActivityManager am = IActivityManager.Stub.asInterface(b);
                        return am;
                    }
                };
        ```
        * Singleton在第一次调用get方法时，回调create方法，创建单例。
        * 这个IActivityManager实例，实际上就是ActivityManagerService(继承IActivityManager.Stub)。
        * ActivityManagerService是服务器返回的Binder对象。
    * 通过ActivityManagerService进行startActivity的IPC调用。
    * 之后在进行checkStartActivityResult：检查Activity启动是否正常(如抛出Manifest中未注册的异常)。
3. AMS::startActivity，此处进行多次跳转、桥接：
    ```
    public final int startActivity(IApplicationThread caller, String callingPackage,
                Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
                int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,
                resultWho, requestCode, startFlags, profilerInfo, bOptions,
                UserHandle.getCallingUserId());
    }

    public final int startActivityAsUser(IApplicationThread caller, String callingPackage,
                Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
                int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        enforceNotIsolatedCaller("startActivity");
        userId = mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(),
                userId, false, ALLOW_FULL_ONLY, "startActivity", null);
        // TODO: Switch to user app stacks here.
        return mActivityStarter.startActivityMayWait(caller, -1, callingPackage, intent,
                resolvedType, null, null, resultTo, resultWho, requestCode, startFlags,
                profilerInfo, null, null, bOptions, false, userId, null, null,
                "startActivityAsUser");
    }
    ```
    * 桥接到ActivityStarter::startActivityMayWait(Api26是这个类，曾经是ActivityStackSupervisor类，但都是桥接)
        * 内部调用：startActivityLocked => startActivity(286) => startActivity(995) => startActivityUnchecked
    * 桥接到ActivityStackSupervisor::resumeFocusedStackTopActivityLocked
    * 桥接到ActivityStack::resumeTopActivityUncheckedLocked => resumeTopActivityInnerLocked
        * 先调用startPausingLocked方法，尝试pause当前Activity
            ```
            prev.app.thread.schedulePauseActivity(prev.appToken, prev.finishing,
                    userLeaving, prev.configChangeFlags, pauseImmediately);
            ```
        * 这里会尝试resume目标Activity，如果无法resume，则代表需要重新启动这个Activity
    * 重新启动：桥接回ActivityStackSupervisor::startSpecificActivityLocked(2633/2663) => realStartActivityLocked
        ```
        void startSpecificActivityLocked(ActivityRecord r,
                    boolean andResume, boolean checkConfig) {
            ...
            if (app != null && app.thread != null) {
                try {
                    ...
                    realStartActivityLocked(r, app, andResume, checkConfig);
                    return;
                } catch (RemoteException e) { ... }
            }
            ...
        }
        ```
4. ActivityStackSupervisor::realStartActivityLocked:
    ```
     app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken,
            System.identityHashCode(r), r.info,
            mergedConfiguration.getGlobalConfiguration(),
            mergedConfiguration.getOverrideConfiguration(), r.compat,
            r.launchedFromPackage, task.voiceInteractor, app.repProcState, r.icicle,
            r.persistentState, results, newIntents, !andResume,
            mService.isNextTransitionForward(), profilerInfo);
    ```
    * app.thread类型为：IApplicationThread
    * 本次操作是一次IPC。

##### IApplicationThread实现：

IApplicationThread的实现是—— ActivityThread的内部类ApplicationThread：
```
private class ApplicationThread extends IApplicationThread.Stub
```
继续上一部分IApplicationThread::scheduleLaunchActivity：
1. IApplicationThread中定义了几乎所有四大组件的操作：
    * 如：schedule-Pause/Stop/Launch/ReLaunch/Resume/Destroy/-Activity等
    * scheduleReceiver、scheduleBindService、scheduleUnbindService、scheduleCreateService
2. 通过服务端IApplicationThread接口的代理Proxy([见AIDL](../AIDL/AIDL.md))，进行IPC操作，回调给客户端的ApplicationThread。

ApplicationThread中相关方法简析：
1. ApplicationThread::scheduleLaunchActivity：
    ```
    public final void scheduleLaunchActivity(......){
        ActivityClientRecord r = new ActivityClientRecord();
        ...
        sendMessage(H.LAUNCH_ACTIVITY, r);
    }
    private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
        Message msg = Message.obtain();
        ...
        mH.sendMessage(msg);
    }
    ```
    * 构建一个ActivityClientRecord，发送消息给mH，ActivityThread的内部类，Handler子类。
2. H::handleMessage：
    ```
    case LAUNCH_ACTIVITY: {
        final ActivityClientRecord r = (ActivityClientRecord) msg.obj;

        r.packageInfo = getPackageInfoNoCheck(
                r.activityInfo.applicationInfo, r.compatInfo);
        handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
    } break;
    ```
3. ActivityThread::handleLaunchActivity：
    ```
    private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent, String reason) {
        unscheduleGcIdler();
        ...
        WindowManagerGlobal.initialize();

        Activity a = performLaunchActivity(r, customIntent);

        if (a != null) {
            r.createdConfig = new Configuration(mConfiguration);
            reportSizeConfigurations(r);
            Bundle oldState = r.state;
            handleResumeActivity(r.token, false, r.isForward,
                    !r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);
            ...
        } else {
            try {
                ActivityManager.getService().finishActivity(...);
            }
        }
    }
    ```
    * 通过performLaunchActivity构建Activity实例。
        1. 从ActivityClientRecord中获取带启动Activity的组件信息
        2. 创建ContextImpl实例：
            ```
            ContextImpl appContext = createBaseContextForActivity(r);
            ```
        3. 创建Activity实例：
            ```
            Activity activity = null;
            try {
                java.lang.ClassLoader cl = appContext.getClassLoader();//ContextImpl::getClassLoader
                activity = mInstrumentation.newActivity(
                        cl, component.getClassName(), r.intent);
            }
            ```
            * 通过Instrumentation::newActivity创建Activity实例
        4. 获取Application实例：
            ```
            LoadedApk::makeApplication
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);
            ```
            * 通过Instrumentation::newApplication创建单例。
        5. 回调Activity::attach，完成初始化：
            ```
            activity.attach(appContext, this, getInstrumentation(), r.token,
                    r.ident, app, r.intent, r.activityInfo, title, r.parent,
                    r.embeddedID, r.lastNonConfigurationInstances, config,
                    r.referrer, r.voiceInteractor, window, r.configCallback);
            ```
            * Activity::attach，调用ContextWrapper::attachBaseContext，将ContextImpl赋值给mBase。
        6. Instrumentation::callActivityOnCreate回调Activity的onCreate方法。
        7. Activity::performStart回调 => Instrumentation::callActivityOnStart(this) => Activity::onStart
    * performResumeActivity回调Activity的onResume。
        * Activity::performResume => Instrumentation::callActivityOnResume(this) => Activity::onResume