### ContentProvider工作过程

#### 从结果说

启动过程：
1. ActivityThread::main 内部调用 ActivityThread::attach
	* 提供ApplicationThread实例作为Binder，调用AMS::attachApplication => AMS::attachApplicationLocked
	* 进行IPC调用: thread.bindApplication
2. ApplicationThread::bindApplication 跳转至 ActivityThread::handleBindApplication
3. ActivityThread::handleBindApplication
	1. 创建ContextImpl实例，创建Instrumentation实例并初始化
	2. 创建Application实例，注意，此时Application没有调用onCreate
	3. 启动当前进程的ContentProvider => installContentProviders
4. ContentProvider相关过程
	1. ActivityThread::installContentProviders
		* 内部调用ActivityThread::installProvider，创建Provider实例
		* 创建完，调用ContentProvider::attachInfo，进行初始化
		* attachInfo会调用ContentProvider::onCreate，此时Provider启动完毕
5. 回到ActivityThread::handleBindApplication
	* ContentProvider启动完毕之后
	* 执行Instrumentation::onCreate
	* 执行Instrumentation::callApplicationOnCreate，调用Application::onCreate

操作，以query为例：
0. 首先，我们需要ContentResolver，用它来执行query
1. ContextWrapper::getContentResolver => ContextImpl::getContentResolver
	* 此方法返回ContextImpl的成员mContentResolver，类型是ContextImpl.ApplicationContentResolver
	* 这个成员在ContextImpl的构造方法中通过new关键字创建
2. 现在我们进行query: ApplicationContentResolver::query
	* 需要一个IContentProvider来进行真正的query操作：IContentProvider unstableProvider = acquireUnstableProvider(uri);
	* ApplicationContentResolver::acquireUnstableProvider => ActivityThread::acquireProvider
3. ActivityThread::acquireProvider
	1. 线通过ActivityThread::acquireExistingProvider，查找mProviderMap中是否已存在目标Provider。
	2. 如果不存在，通过AMS::getContentProvider，启动目标ContentProviderHolder(启动过程也会回到ActivityThread中，与之前的启动基本相同)
	3. 拿到AMS通过IPC获得的Holder之后，通过ActivityThread::installProvider，使用Holder创建、更新Provider(与启动过程相同)
	4. 将Holder中的IContentProvider返回
4. 关于IContentProvider的实现
	* 通过ActivityThread::installProvider可知，真实的IContentProvider实现是ContentProvider.Transport
	* Transport继承了ContentProviderNative，后者继承Binder实现了IContentProvider。
5. 实际的query：
	* ContentProvider.Transport::query => ContentProvider::query
	* 最终调用到抽象的query方法，也就是客户端对ContentProvider的query实现。

###### ContentProvider的启动过程

0. ActivityThread::main => ActivityThread::attach
    1. 提供ApplicationThread实例作为Binder，专递给AMS::attachApplication => AMS::attachApplicationLocked => IApplicationThread::bindApplication
    2. ApplicationThread::bindApplication => ActivityThread::handleBindApplication
1. ActivityThread::handleBindApplication中与Application、ContentProvider相关代码：
    1. 创建ContextImpl、Instrumentation
        ```
        //创建ContextImpl
        final LoadedApk pi = getPackageInfo(instrApp, data.compatInfo,
            appContext.getClassLoader(), false, true, false);
        final ContextImpl instrContext = ContextImpl.createAppContext(this, pi);

        //创建Instrumentation
        try {
            final ClassLoader cl = instrContext.getClassLoader();
            mInstrumentation = (Instrumentation)
                cl.loadClass(data.instrumentationName.getClassName()).newInstance();
        } catch (Exception e) { ... }

        //初始化Instrumentation
        final ComponentName component = new ComponentName(ii.packageName, ii.name);
        mInstrumentation.init(this, instrContext, appContext, component,
            data.instrumentationWatcher, data.instrumentationUiAutomationConnection);
        ```
    2. 创建Application实例
        ```
        //通过LoadedApk::makeApplication创建实例
        Application app = data.info.makeApplication(data.restrictedBackupMode, null);
        mInitialApplication = app;
        ```
    3. 启动当前进程的ContentProvider：
        ```
        if (!data.restrictedBackupMode) {
            if (!ArrayUtils.isEmpty(data.providers)) {
                installContentProviders(app, data.providers);

                mH.sendEmptyMessageDelayed(H.ENABLE_JIT, 10*1000);
            }
        }

        private void installContentProviders(
                Context context, List<ProviderInfo> providers) {
            final ArrayList<ContentProviderHolder> results = new ArrayList<>();

            for (ProviderInfo cpi : providers) {
                if (DEBUG_PROVIDER) { ... }
                ContentProviderHolder cph = installProvider(context, null, cpi,
                        false /*noisy*/, true /*noReleaseNeeded*/, true /*stable*/);
                if (cph != null) {...}
            }

            try {
                ActivityManager.getService().publishContentProviders(
                    getApplicationThread(), results);
            } catch (RemoteException ex) { ... }
        }
        ```
        * 启动Provider在installProvider中：
            ```
            private ContentProviderHolder installProvider(Context context,
                    ContentProviderHolder holder, ProviderInfo info,
                    boolean noisy, boolean noReleaseNeeded, boolean stable) {
                关键代码：
                //创建Provider实例
                final java.lang.ClassLoader cl = c.getClassLoader();
                localProvider = (ContentProvider)cl.
                    loadClass(info.name).newInstance();
                //此处返回IContentProvider的Binder实现类： ContentProvider.Transport
                provider = localProvider.getIContentProvider();
                if (provider == null) {
                    Slog.e(TAG, "Failed to instantiate class " +
                          info.name + " from sourceDir " +
                          info.applicationInfo.sourceDir);
                    return null;
                }
                if (DEBUG_PROVIDER) Slog.v(...);
                //初始化Provider
                localProvider.attachInfo(c, info);
                ...
                //将ContentProviderHolder保存在本地的容器中
            }
            ```
            * 创建Provider实例之后，调用ContentProvider::attachInfo
            * 最终调用ContentProvider::onCreate，此时ContentProvider启动完毕
    4. 启动完毕ContentProvider之后，才会执行Application::onCreate
        ```
        try {
            mInstrumentation.onCreate(data.instrumentationArgs);
        }
        catch (Exception e) { ... }

        try {
            mInstrumentation.callApplicationOnCreate(app);
        } catch (Exception e) { ... }

        public void Instrumentation.callApplicationOnCreate(Application app) {
            app.onCreate();
        }
        ```

###### ContentProvider的调用过程

从ContextWrapper::getContentResolver
1. ContextImpl::getContentResolver
    ```
    public ContentResolver getContentResolver() {
        return mContentResolver;
    }

    private ContextImpl(...){
        ...
        mContentResolver = new ApplicationContentResolver(this, mainThread, user);
    }
    ```
2. ApplicationContentResolver::query => unstableProvider.query
    * IContentProvider unstableProvider = acquireUnstableProvider(uri);
    * ApplicationContentResolver::acquireUnstableProvider => ActivityThread::acquireProvider
    ```
    public final IContentProvider ActivityThread.acquireProvider(
            Context c, String auth, int userId, boolean stable) {
        final IContentProvider provider = acquireExistingProvider(c, auth, userId, stable);
        if (provider != null) {
            return provider;
        }
        ContentProviderHolder holder = null;
        try {
            holder = ActivityManager.getService().getContentProvider(
                    getApplicationThread(), auth, userId, stable);
        } catch (RemoteException ex) { ... }
        if (holder == null) {
            Slog.e(TAG, "Failed to find provider info for " + auth);
            return null;
        }

        // Install provider will increment the reference count for us, and break
        // any ties in the race.
        holder = installProvider(c, holder, holder.info,
                true /*noisy*/, holder.noReleaseNeeded, stable);
        return holder.provider;
    }
    ```
    1. 先通过acquireExistingProvider，查找ActivityThread的mProviderMap中是否已存在这个Provider
    2. 如果不存在，通过AMS::getContentProvider启动目标Provider
    3. 通过installProvider修改修改引用计数
3. 从ContentProvider启动过程中，已知：
    1. installProvider中，通过localProvider.getIContentProvider()获取实际的Provider实例
    2. 这个实例就是：ContentProvider.Transport
        ```
        class Transport extends ContentProviderNative
        abstract public class ContentProviderNative extends Binder implements IContentProvider
        ```
    3. 于是，ContentResolver::query，最终会通过ContentProvider.Transport::query
    4. ContentProvider.Transport::query => ContentProvider::query
    5. 最终走向抽象的query，也就是客户端对ContentProvider的query实现。