### Activity的Window创建

此处需要了解Activity启动过程，[具体在此](../../../../Official&Basic/IPC/ComponentWorkFlow/ActivityWorkFlow.md)

##### 流程总结

0. startActivity的前戏：
	1. ActivityThread::performLaunchActivity => Activity::attach
	2. 上述方法中，创建PhoneWindow实例作为Activity的mWindow
1. setContentView：将mWindow与Activity视图关联
	* 桥接至PhoneWindow::setContentView
	1. 首先如果没有DecorView，通过installDecor创建DecorView，并将其与PhoneWindow关联
		* DecorView是一个FrameLayout，Activity的顶级View
		* 内部一定包含一个Content，id为：android.R.id.content
		* 通过generateLayout，将对应主题、Api版本的LayoutRes，加载到DecorView中
		* 将DecorView中Content部分，作为PhoneWindow的mContentParent。
	2. 将LayoutRes添加到DecorView的mContentParent中。
	3. 回调Activity的onContentChanged方法，通知Activity视图改变
2. 上述PhoneWindow::setContentView的3个步骤之后
	* DecorView已经被创建、初始化完毕，Activity的LayoutRes成功加载到mContentParent中。
	* 但此时DecorView还没有被WindowManager添加到Window中。
	* 需要等到ActivityThread::handleResumeActivity方法
		1. 调用Activity::onResume => Activity::makeVisible方法
		2. makeVisible方法中，会调用WindowManager::addView，添加View到Window中。

#### 具体流程

下面我们挑简要流程讲解：
1. ActivityThread::performLaunchActivity:
    1. 创建Activity
        ```
        ContextImpl appContext = createBaseContextForActivity(r);
        Activity activity = null;
        try {
            java.lang.ClassLoader cl = appContext.getClassLoader();
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
            ...
        } catch (Exception e) {...}
        ```
    2. 调用Activity::attach
        ```
        if (activity != null) {
            CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
            Configuration config = new Configuration(mCompatConfiguration);
            ...
            Window window = null;
            if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                window = r.mPendingRemoveWindow;
                r.mPendingRemoveWindow = null;
                r.mPendingRemoveWindowManager = null;
            }
            appContext.setOuterContext(activity);
            activity.attach(appContext, this, getInstrumentation(), r.token,
                    r.ident, app, r.intent, r.activityInfo, title, r.parent,
                    r.embeddedID, r.lastNonConfigurationInstances, config,
                    r.referrer, r.voiceInteractor, window, r.configCallback);
            ...
        }
        ```
2. Activity::attach：
    1. 与Window进行关联
        ```
        mWindow = new PhoneWindow(this, window, activityConfigCallback);//Window的唯一实现类：PhoneWindow
        mWindow.setWindowControllerCallback(this);
        mWindow.setCallback(this);
        mWindow.setOnWindowDismissedCallback(this);
        mWindow.getLayoutInflater().setPrivateFactory(this);
        if (info.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            mWindow.setSoftInputMode(info.softInputMode);
        }
        if (info.uiOptions != 0) {
            mWindow.setUiOptions(info.uiOptions);
        }
        ...
        mWindow.setWindowManager(
                (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
                mToken, mComponent.flattenToString(),
                (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
        if (mParent != null) {
            mWindow.setContainer(mParent.getWindow());
        }
        mWindowManager = mWindow.getWindowManager();
        mCurrentConfig = config;

        mWindow.setColorMode(info.colorMode);
        ```

#### Activity的视图与Window的关联 —— setContentView方法
方法体：
```
public void setContentView(@LayoutRes int layoutResID) {
    getWindow().setContentView(layoutResID);
    initWindowDecorActionBar();
}
```
1. getWindow：
    ```
    public Window getWindow() {
        return mWindow;
    }
    ```
    * 返回mWindow，也就是一个PhoneWindow实例
2. PhoneWindow::setContentView
    ```
    public void setContentView(int layoutResID) {
        // Note: FEATURE_CONTENT_TRANSITIONS may be set in the process of installing the window
        // decor, when theme attributes and the like are crystalized. Do not check the feature
        // before this happens.

        //step1:
        if (mContentParent == null) {
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }
        //step2:
        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
                    getContext());
            transitionTo(newScene);
        } else {
            mLayoutInflater.inflate(layoutResID, mContentParent);
        }
        //step3:
        mContentParent.requestApplyInsets();
        final Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        mContentParentExplicitlySet = true;
    }
    ```
    1. 如果没有DecorView，就创建DecorView，installDecor方法，关联DecorView与PhoneWindow：
        ```
        private void installDecor(){
            mForceDecorInstall = false;
            if (mDecor == null) {
                mDecor = generateDecor(-1);// new DecorView(context, featureId, this<PhoneWindow>, getAttributes())
                mDecor.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                mDecor.setIsRootNamespace(true);
                if (!mInvalidatePanelMenuPosted && mInvalidatePanelMenuFeatures != 0) {
                    mDecor.postOnAnimation(mInvalidatePanelMenuRunnable);
                }
            } else {
                mDecor.setWindow(this<PhoneWindow>);
            }
            if (mContentParent == null) {
                mContentParent = generateLayout(mDecor);
                ...
            }
        }
        ```
        * DecorView是一个FrameLayout，Activity中的顶级View。
        * 内部肯定包含一个内容栏(主题不同，还可能有标题栏)，内容栏的id为android.R.id.content
        * 通过generateLayout，加载对应主题/Api版本的布局到DecorView中
            ```
            public static final int ID_ANDROID_CONTENT = com.android.internal.R.id.content;
            ViewGroup contentParent = (ViewGroup)findViewById(ID_ANDROID_CONTENT);
            ```
    2. 将View添加到DecorView的mContentParent中
    3. 回调Activity的onContentChanged方法，通知Activity视图改变
3. PhoneWindow::setContentView的3个步骤之后：
    * DecorView已经被创建并初始化完毕，Activity的布局文件成功添加到DecorView的mContentParent中
    * 但此时DecorView还没有被WindowManager添加到Window中。
    * 需要等到ActivityThread::handleResumeActivity方法：
        1. 调用Activity::onResume方法
        2. 调用Activity::makeVisible方法
            ```
            void makeVisible() {
                if (!mWindowAdded) {
                    ViewManager wm = getWindowManager();
                    wm.addView(mDecor, getWindow().getAttributes());
                    mWindowAdded = true;
                }
                mDecor.setVisibility(View.VISIBLE);
            }
            ```
            * 此处，mDecor才被WindowManager::addView方法添加
