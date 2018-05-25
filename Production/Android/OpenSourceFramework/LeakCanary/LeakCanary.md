### LeakCanary

以下基于1.5.4版本简析流程

#### 构建Watcher、启动Watcher

1. 入口：
    ```
        //默认初始化模式
        public static RefWatcher install(Application application) {
            return refWatcher(application)//创建WatcherBuilder
              .listenerServiceClass(DisplayLeakService.class)//设置监听服务，要求是AbstractAnalysisResultService子类(本身继承IntentService，用于通信)
              .excludedRefs(AndroidExcludedRefs.createAppDefaults().build())//添加忽略名单，可以添加你已知的特定引用，保证该引用不会被认为是泄露。
              .buildAndInstall();//构建Watcher并install
        }
        //也可以自行对RefWatcher进行定制。具体定制流程之后再提。
        public static AndroidRefWatcherBuilder refWatcher(Context context) {
            return new AndroidRefWatcherBuilder(context);
        }
    ```
2. buildAndInstall：
    ```
    public RefWatcher buildAndInstall() {
        RefWatcher refWatcher = build();
        if (refWatcher != DISABLED) {
            //开启DisplayLeakActivity
          LeakCanary.enableDisplayLeakActivity(context);
          ActivityRefWatcher.install((Application) context, refWatcher);//启动Watcher
        }
        return refWatcher;
    }
    //install
    public static void install(Application application, RefWatcher refWatcher) {
        new ActivityRefWatcher(application, refWatcher).watchActivities();
    }
    void onActivityDestroyed(Activity activity) {
        refWatcher.watch(activity);
    }
    ```
    * install过程就是对目标Application注册ActivityLifecycleCallbacks
    * 该ActivityLifecycleCallbacks只重写了onActivityDestroyed，调用ActivityRefWatcher.this.onActivityDestroyed(activity)
    * 实际上就是调用了RefWatcher::watch(Activity)
    * 注意：
        1. RefWatcherBuilder::build方法会判断当前子类的isDisabled()方法返回值，如果返回true，则得到一个RefWatcher.DISABLED
        2. AndroidRefWatcherBuilder::isDisabled方法调用了LeakCanary::isInAnalyzerProcess，判断App是否在Service中进行启动

#### 泄露监控

1. watch，如何判断泄露：
    ```
    public void watch(Object watchedReference, String referenceName) {
        if (this == DISABLED) {
        return;
        }
        checkNotNull(watchedReference, "watchedReference");
        checkNotNull(referenceName, "referenceName");
        final long watchStartNanoTime = System.nanoTime();
        String key = UUID.randomUUID().toString();
        retainedKeys.add(key);
        final KeyedWeakReference reference =
          new KeyedWeakReference(watchedReference, key, referenceName, queue);

        ensureGoneAsync(watchStartNanoTime, reference);//会在对应的WatchExecutor中执行，并不会阻塞
    }
    ```
    * WeakReference与ReferenceQueue
        1. WeakReference的重载构造：public WeakReference(T referent, ReferenceQueue<? super T> q)
        2. KeyedWeakReference将watchedReference引用与ReferenceQueue<Object> queue这个队列关联
        3. 当系统准备回收目标Reference中的引用时，Queue会对当前Reference进行入队操作。Queue中的元素将是Reference<T>实例
    * retainedKeys是一个Set<String>，实际类型是CopyOnWriteArraySet
        1. 使用UUID对引用进行身份标记
        2. 如果引用被成功GC，则从Set中移除该标记
        3. 在允许时间内，还残留在Set中的Key，其对应的引用，被视为泄露
2. EnsureGone：判断引用可以被GC
    ```
    //在对应WatchExecutor中执行ensureGone方法
    private void ensureGoneAsync(final long watchStartNanoTime, final KeyedWeakReference reference) {
        watchExecutor.execute(new Retryable() {
        @Override public Retryable.Result run() {
                return ensureGone(reference, watchStartNanoTime);
            }
        });
    }
    Retryable.Result ensureGone(final KeyedWeakReference reference, final long watchStartNanoTime) {
        long gcStartNanoTime = System.nanoTime();
        long watchDurationMs = NANOSECONDS.toMillis(gcStartNanoTime - watchStartNanoTime);
        //先清空ReferenceQueue
        removeWeaklyReachableReferences();

        if (debuggerControl.isDebuggerAttached()) {//如果处于Debug模式
        // The debugger can create false leaks.//由于Debugger可能产生错误的泄露，返回RETRY，在此执行本方法
        return RETRY;
        }
        if (gone(reference)) {//判断此reference是否已经被GC
        return DONE;//如果已经被GC，返回GC完成标记
        }
        gcTrigger.runGc();//GC触发器，尝试GC
        removeWeaklyReachableReferences();//尝试GC之后，再次清空Queue，并刷新残留Key
        if (!gone(reference)) {//如果目标reference仍然没有回收，则认为已经产生泄露
        long startDumpHeap = System.nanoTime();
        long gcDurationMs = NANOSECONDS.toMillis(startDumpHeap - gcStartNanoTime);

        File heapDumpFile = heapDumper.dumpHeap();//尝试进行dump
        if (heapDumpFile == RETRY_LATER) {
          // Could not dump the heap.
          return RETRY;//如果当前HeapDumper返回了RETRY_LATER，则考虑再次RETRY。
        }
        long heapDumpDurationMs = NANOSECONDS.toMillis(System.nanoTime() - startDumpHeap);
        //执行到这里，会输出内存的泄露快照
        heapdumpListener.analyze(
            new HeapDump(heapDumpFile, reference.key, reference.name, excludedRefs, watchDurationMs,
                gcDurationMs, heapDumpDurationMs));
        }
        return DONE;
    }
    //被GC的实例的Key将被移出RetainedKeys容器，此处判断容器中是否有该Key即可
    private boolean gone(KeyedWeakReference reference) {
        return !retainedKeys.contains(reference.key);
    }
    //取出Queue中所有的元素(在Queue中，证明即将被GC)，并清除其在RetainedKeys中的标记
    private void removeWeaklyReachableReferences() {
        // WeakReferences are enqueued as soon as the object to which they point to becomes weakly
        // reachable. This is before finalization or garbage collection has actually happened.
        KeyedWeakReference ref;
        while ((ref = (KeyedWeakReference) queue.poll()) != null) {
        retainedKeys.remove(ref.key);
        }
    }
    ```
    * ensureGone方法会被WatchExecutor多次调用，根据返回的是DONE还是RETRY决定是否再次对同一引用进行分析
    1. 首先清空ReferenceQueue
        1. 先确定是否处于Debug模式，是则直接RETRY
        2. 然后判断引用是否已经被回收，已被回收则流程结束
    2. 如果没被回收，尝试触发GC，然后再次判断是否已被回收，已被回收则流程结束
    3. GC之后如果仍然没被回收，则通过HeapDumper尝试获取dumpFile
        * android.os.Debug.dumpHprofData(heapDumpFile.getAbsolutePath())，通过这个官方API进行HeapDump
        1. 如果HeapDumper表示无法进行dump，则继续走RETRY
        2. 如果没问题，通过HeapDumpListener::analyze进行快照输出
3. AndroidWatchExecutor实现
    ```
    //实际的Retryable会在这个方法中被传递、执行
    @Override public void execute(Retryable retryable) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
        waitForIdle(retryable, 0);
        } else {
        postWaitForIdle(retryable, 0);
        }
    }
    //当前非主线程，则使用主线程的Handler进行post(Runnable)，调用waitForIdle
    void postWaitForIdle(final Retryable retryable, final int failedAttempts) {
        mainHandler.post(new Runnable() {
        @Override public void run() {
          waitForIdle(retryable, failedAttempts);
        }
        });
    }
    //当前是主线程，则添加一个IdleHandler到主线程的MessageQueue
    //在MessageQueue进入Idle状态时，回调queueIdle，调用postToBackgroundWithDelay方法
    void waitForIdle(final Retryable retryable, final int failedAttempts) {
        // This needs to be called from the main thread.
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
        @Override public boolean queueIdle() {
          postToBackgroundWithDelay(retryable, failedAttempts);
          return false;
        }
        });
    }
    //此处会根据重试的次数，计算postDelay的时间，然后使用backgroundHandler进行postDelayed
    //Runnable中会调用Retryable::run，如果该run方法返回了RETRY，则重复调用postWaitForIdle(因为是backgroundHandler，不在主线程)。
    void postToBackgroundWithDelay(final Retryable retryable, final int failedAttempts) {
        long exponentialBackoffFactor = (long) Math.min(Math.pow(2, failedAttempts), maxBackoffFactor);
        long delayMillis = initialDelayMillis * exponentialBackoffFactor;
        backgroundHandler.postDelayed(new Runnable() {
        @Override public void run() {
          Retryable.Result result = retryable.run();
          if (result == RETRY) {
            postWaitForIdle(retryable, failedAttempts + 1);
          }
        }
        }, delayMillis);
    }
    ```
    1. WatchExecutor::execute的默认Android实现会根据调用线程不同进入两个分支，但都会汇入waitForIdle
    2. waitForIdle给主线程的MessageQueue添加一个IdleHandler回调
        1. 这个回调会在MessageQueue::next方法中，由于当前无消息、队列处于Idle状态而被回调
        2. 回调之后，根据返回值是true/false，决定保留/移除这个IdleHandler
    3. IdleHandler回调中调用postToBackgroundWithDelay，并返回false。
    4. postToBackgroundWithDelay中使用backgroundHandler(通过HandlerThread创建)进行postDelayed
        1. 先根据重试次数计算本次delay的时间
        2. Runnable中执行Retryable::run
        3. 如果返回的结果是RETRY，再次调用postWaitForIdle，会在下一次IdleHandler回调时，再次进入本方法
#### 快照输出
1. heapDumpListener.analyze(HeapDump)
    ```
    在构建RefWatcher时有这个添加ListenerService的代码：
    listenerServiceClass(DisplayLeakService.class)

    //实际上，通过内部调用heapDumpListener，将listenerServiceClass与HeapDump.Listener关联
    //并添加Listener给RefWatcher
    public AndroidRefWatcherBuilder listenerServiceClass(
        Class<? extends AbstractAnalysisResultService> listenerServiceClass) {
        return heapDumpListener(new ServiceHeapDumpListener(context, listenerServiceClass));
    }
    @Override public void analyze(HeapDump heapDump) {
        checkNotNull(heapDump, "heapDump");
        HeapAnalyzerService.runAnalysis(context, heapDump, listenerServiceClass);
    }
    ```
    * 调用此方法后，会进行一次IntentService的调用
2. HeapAnalyzerService，是一个IntentService
    ```
    //调用这个方法，实际上就是回调HeapAnalyzerService的onHandleIntent的方法
    public static void runAnalysis(Context context, HeapDump heapDump,
        Class<? extends AbstractAnalysisResultService> listenerServiceClass) {
        Intent intent = new Intent(context, HeapAnalyzerService.class);
        intent.putExtra(LISTENER_CLASS_EXTRA, listenerServiceClass.getName());
        intent.putExtra(HEAPDUMP_EXTRA, heapDump);
        context.startService(intent);
    }
    @Override protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            CanaryLog.d("HeapAnalyzerService received a null intent, ignoring.");
            return;
        }
        String listenerClassName = intent.getStringExtra(LISTENER_CLASS_EXTRA);
        HeapDump heapDump = (HeapDump) intent.getSerializableExtra(HEAPDUMP_EXTRA);
        //创建HeapAnalyzer，并忽略部分无效泄露
        HeapAnalyzer heapAnalyzer = new HeapAnalyzer(heapDump.excludedRefs);
        //通过HeapAnalyzer进行Leak检验
        AnalysisResult result = heapAnalyzer.checkForLeak(heapDump.heapDumpFile, heapDump.referenceKey);
        //通过IntentService发送结果给listenerClassName
        AbstractAnalysisResultService.sendResultToListener(this, listenerClassName, heapDump, result);
    }
    ```
    1. HeapAnalyzerService是IntentService
    2. onHandleIntent中会通过HeapAnalyzer::checkForLeak进行泄露的分析
        * 具体的check细节、结果的解析，使用了方块公司的另一个开源库 ~ Headless Android Heap Analyzer(haha)
    3. 分析结果通过AbstractAnalysisResultService，统一发送给实现类
3. AbstractAnalysisResultService
    ```
    //AbstractAnalysisResultService是个抽象类，
    //由之前的入口方法，保证此处的listenerServiceClassName，都是AbstractAnalysisResultService的子类
    public static void sendResultToListener(Context context, String listenerServiceClassName,
        HeapDump heapDump, AnalysisResult result) {
        Class<?> listenerServiceClass;
        try {
            listenerServiceClass = Class.forName(listenerServiceClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Intent intent = new Intent(context, listenerServiceClass);
        intent.putExtra(HEAP_DUMP_EXTRA, heapDump);
        intent.putExtra(RESULT_EXTRA, result);
        context.startService(intent);
    }
    //所有子类通过sendResultToListener，最终汇入onHandleIntent方法
    //实际处理在onHeapAnalyzed的实现中
    @Override protected final void onHandleIntent(Intent intent) {
        HeapDump heapDump = (HeapDump) intent.getSerializableExtra(HEAP_DUMP_EXTRA);
        AnalysisResult result = (AnalysisResult) intent.getSerializableExtra(RESULT_EXTRA);
        try {
            onHeapAnalyzed(heapDump, result);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            heapDump.heapDumpFile.delete();
        }
    }
    ```
    1. 实现类中通过onHeapAnalyzed的实现，处理泄露分析结果
4. DisplayLeakService。由入口参数可知，最终会汇入此类中
    * 所有处理都在@Override protected final void onHeapAnalyzed(HeapDump heapDump, AnalysisResult result)方法中
    1. 通过AnalysisResult，检测结果来判定是否需要进行泄露处理
    2. 如果需要处理，则先会对HeapDump进行重命名，并写入到本地File中
    3. 根据几种判断结果，生成Notification需要的各种参数
        * 可能会跳转DisplayLeakActivity
        * DisplayLeakActivity.createPendingIntent创建pendingIntent
    4. 显示Notification()
    5. 可以选择重写afterDefaultHandling，对泄露结果进行自定义处理(如上传等)