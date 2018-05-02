### Window的更新过程

#### WindowManagerGlobal::updateViewLayout
完整方法体：
```
public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
    if (view == null) {
        throw new IllegalArgumentException("view must not be null");
    }
    if (!(params instanceof WindowManager.LayoutParams)) {
        throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
    }

    final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams)params;

    view.setLayoutParams(wparams);

    synchronized (mLock) {
        int index = findViewLocked(view, true);
        ViewRootImpl root = mRoots.get(index);
        mParams.remove(index);
        mParams.add(index, wparams);
        root.setLayoutParams(wparams, false);
    }
}
```
1. 首先，更新View的LayoutParams，并替换掉旧的LayoutParams
2. 接着更新ViewRootImpl中的LayoutParams：root.setLayoutParams(wparams,false)
3. ViewRootImpl::setLayoutParams中通过scheduleTraversals方法，对View进行重新布局(测量、布局、重绘)
    * scheduleTraversals => performTraversals => relayoutWindow => mWindowSession.relayout
    * Session::reLayout方法中，通过WindowManagerService::relayoutWindow，进行Window视图的更新，IPC