### Window的删除过程

#### WindowManagerGlobal::removeView():
完整方法：
```
public void removeView(View view, boolean immediate/*是否立即删除*/) {
    if (view == null) {
        throw new IllegalArgumentException("view must not be null");
    }

    synchronized (mLock) {
        int index = findViewLocked(view, true);
        View curView = mRoots.get(index).getView();
        removeViewLocked(index, immediate);
        if (curView == view) {
            return;
        }

        throw new IllegalStateException("Calling with view " + view
                + " but the ViewAncestor is attached to " + curView);
    }
}
```

1. 首先通过findViewLocked查找待删除的View的索引(O(N)的List遍历查找)
    ```
    private int findViewLocked(View view, boolean required) {
        final int index = mViews.indexOf(view);
        if (required && index < 0) {
            throw new IllegalArgumentException("View=" + view + " not attached to window manager");
        }
        return index;
    }
    ```
2. 找到之后，调用removeViewLocked进行删除
    ```
    private void removeViewLocked(int index, boolean immediate) {
        ViewRootImpl root = mRoots.get(index);
        View view = root.getView();

        if (view != null) {
            InputMethodManager imm = InputMethodManager.getInstance();
            if (imm != null) {
                imm.windowDismissed(mViews.get(index).getWindowToken());
            }
        }
        boolean deferred = root.die(immediate);
        if (view != null) {
            view.assignParent(null);
            if (deferred) {
                mDyingViews.add(view);
            }
        }
    }
    ```
3. removeViewLocked通过ViewRootImpl来完成删除操作
    * WindowManager中，提供removeView和removeViewImmediate，分别表示异步/同步删除
    * 其中，removeViewImmediate(同步删除)使用要特别注意(一般不需要使用此方法来删除Window，以免发生意外)
    * 主要讲异步删除，通过ViewRootImpl的die方法完成：
        ```
        boolean die(boolean immediate) {
            // Make sure we do execute immediately if we are in the middle of a traversal or the damage
            // done by dispatchDetachedFromWindow will cause havoc on return.
            if (immediate && !mIsInTraversal) {
                doDie();//同步删除，直接阻塞调用doDie方法
                return false;
            }

            if (!mIsDrawing) {
                destroyHardwareRenderer();
            } else {
                Log.e(mTag, "Attempting to destroy the window while drawing!\n" +
                        "  window=" + this + ", title=" + mWindowAttributes.getTitle());
            }
            mHandler.sendEmptyMessage(MSG_DIE);//发送消息，handleMessage中调用doDie方法
            return true;
        }
        ```
        * 异步删除情况下：die方法只是发送了一个请求删除的消息，然后就立即返回了一个true
        * 此时，View并没有完成删除操作
        * 最后会将View添加到mDyingViews(removeView没有完成的View的列表)中(见第2条的block)
4. View真正的被删除
    * doDie内部调用dispatchDetachedFromWindow方法(真正删除View的逻辑在此方法内实现)
    * dispatchDetachedFromWindow效果：
        1. 垃圾回收相关工作，如：清除数据和消息、移除回调
        2. 通过Session的remove方法删除Window：mWindowSession.remove(mWindow);
            ```
            public void remove(IWindow window) {
                    mService.removeWindow(this, window);
                }
            ```
            * IPC过程，调用WindowManagerService的removeWindow方法
        3. 调用View::dispatchDetachedFromWindow
            * 内部调用View::onDetachedFromWindow、View::onDetachedFromWindowInternal
        4. 调用WindowManagerGlobal::doRemoveView方法，刷新数据
            * 包括：mRoots、mParams、mDyingViews
            * 需要将当前Window锁关联的这3个对象，移除列表