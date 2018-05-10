### Window的添加过程

###### 简介
此过程通过WindowManager::addView()方法实现：
* WindowManagerImpl类实现了WindowManager接口
* 其中addView实现的方法体为：
    ```
    Api21:
    mGlobal.addView(view,params,mDisplay,mParentWindow);
    Api27:
    applyDefaultToken(params);
    mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    ```
* 所有操作委托给WindowManagerGlobal处理。
* 剩余2大操作：updateViewLayout、removeView也采取这种<桥接模式>

##### 流程总结

WindowManagerImpl::addView，桥接给WindowManagerGlobal::addView，此方法分以下几步：
1. 检查参数是否合法；如果是子Window(parentWindow != null)，则需要调整一些布局参数
2. 创建ViewRootImpl实例，并将View、ViewImpl、LayoutParams添加到成员变量容器中。
3. 通过ViewRootImpl::setView方法，更新界面、并完成Window的添加过程。
	1. View绘制相关
		1. 上述方法调用ViewRootImpl::requestLayout => ViewRootImpl::scheduleTraversals
		2. 上述方法发送出一个ViewRootImpl.TraversalRunnable实例
		3. 其run方法中调用ViewRootImpl::doTraversal => ViewRootImpl::performTraversals，开始View的绘制流程。
	2. 添加Window，此处发起IPC
		1. 通过IWindowSession::addToDisplay方法，添加Window
		2. 实现类是com.android.server.wm.Session
		3. Session::addToDisplay调用WindowManagerService::addWindow

#### WindowManagerGlobal::addView的实现分为以下3步：

具体签名：
```
public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow)
```
###### 1.检查参数是否合法，如果是子Window(parentWindow != null)，还需要调整一些布局参数
```
if (view == null) {
    throw new IllegalArgumentException("view must not be null");
}
if (display == null) {
    throw new IllegalArgumentException("display must not be null");
}
if (!(params instanceof WindowManager.LayoutParams)) {
    throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
}

final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
if (parentWindow != null) {
    parentWindow.adjustLayoutParamsForSubWindow(wparams);
} else {
    // If there's no parent, then hardware acceleration for this view is
    // set from the application's hardware acceleration setting.
    final Context context = view.getContext();
    if (context != null
            && (context.getApplicationInfo().flags
                    & ApplicationInfo.FLAG_HARDWARE_ACCELERATED) != 0) {
        wparams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
    }
}
```
###### 2.创建ViewRootImpl，并将View添加到列表中
* WindowManagerGlobal的重要成员：
    ```
    private final ArrayList<View> mViews = new ArrayList<View>();//所有Window所对应的View
    private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();//所有Window所对应的ViewRootImpl
    private final ArrayList<WindowManager.LayoutParams> mParams =
            new ArrayList<WindowManager.LayoutParams>();//所有Window所对应的布局参数
    private final ArraySet<View> mDyingViews = new ArraySet<View>();//所有正在被删除的View对象(调用了removeView方法，但删除操作还未完成的Window对象)
    ```
* 添加Window的一系列对象到成员列表中
    ```
    root = new ViewRootImpl(view.getContext(),display);
    view.setLayoutParams(wparams);
    mViews.add(view);
    mRoots.add(root);
    mParams.add(wparams);
    ```
###### 3.通过ViewRootImpl来更新界面、完成Window的添加过程
* 通过：root.setView(view, wparams, panelParentView)来完成
* View的绘制过程是由ViewRootImpl完成的，此处也如此
* setView方法中，调用requestLayout完成异步刷新请求，并开始View的绘制
    * requestLayout：
        ```
        public void requestLayout() {
            if (!mHandlingLayoutInLayoutRequest) {
                checkThread();
                mLayoutRequested = true;
                scheduleTraversals();//此处是View绘制的入口
            }
        }
        ```
    * scheduleTraversals:
        ```
        void scheduleTraversals() {
            if (!mTraversalScheduled) {
                mTraversalScheduled = true;
                mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
                //此处发出一个mTraversalRunnable，其run方法调用doTraversal
                //doTranversal中调用performTraversal，开始View的绘制流
                mChoreographer.postCallback(
                        Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
                if (!mUnbufferedInputDispatch) {
                    scheduleConsumeBatchedInput();
                }
                notifyRendererOfFramePending();
                pokeDrawLockIfNeeded();
            }
        }
        ```
* 再之后setView方法通过WindowSession，最终完成Window的添加过程
    * Window的添加过程，是一次IPC调用：
        ```
        try {
            mOrigWindowType = mWindowAttributes.type;
            mAttachInfo.mRecomputeGlobalAttributes = true;
            collectViewAttributes();
            res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
                    getHostVisibility(), mDisplay.getDisplayId(),
                    mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
                    mAttachInfo.mOutsets, mInputChannel);
        } catch (RemoteException e) {
            mAdded = false;
            mView = null;
            mAttachInfo.mRootView = null;
            mInputChannel = null;
            mFallbackEventHandler.setView(null);
            unscheduleTraversals();
            setAccessibilityFocus(null, null);
            throw new RuntimeException("Adding window failed", e);
        } finally {
            if (restore) {
                attrs.restore();
            }
        }
        ```
        * 上述mWindowSession类型是IWindowSession，是一个Binder对象
        * 真正的实现类，是Session类。
    * Session内部通过WindowManagerService实现Window的添加
        ```
        public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs,
                int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets,
                Rect outOutsets, InputChannel outInputChannel) {
            return mService.addWindow(this, window, seq, attrs, viewVisibility, displayId,
                    outContentInsets, outStableInsets, outOutsets, outInputChannel);
        }
        ```
* 如此，Window的添加请求，就交给WindowManagerService处理
    * WindowManagerService内部会为每一个应用，保留一个单独的Session