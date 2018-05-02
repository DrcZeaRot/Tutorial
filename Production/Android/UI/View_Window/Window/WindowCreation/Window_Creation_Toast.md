### Toast的Window创建

Toast简介：
* Toast与Dialog不同，工作过程更复杂。
* Toast基于Window实现，采用Handler来处理定时取消功能(创建Toast需要传入Looper，传入null则要求创建线程有Looper)。
* Toast内部有2类的IPC：
    1. 第一类：Toast访问NotificationManagerService(NMS)
    2. 第二类：NotificationManagerService回调Toast的TN接口

#### Toast::show/cancel

* Toast属于系统Window，内部视图有两种指定方式：
    1. 系统默认样式
    2. 通过setView方法指定自定义View
* 两种方式都对应Toast的一个成员：View mNextView
* Toast的show、cancel方法，用于显示/隐藏Toast的视图，内部是IPC过程：
    ```
    public void show() {
        if (mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }

        INotificationManager service = getService();
        String pkg = mContext.getOpPackageName();
        TN tn = mTN;
        tn.mNextView = mNextView;

        try {
            service.enqueueToast(pkg, tn, mDuration);//IPC
        } catch (RemoteException e) { }
    }

    public void cancel() {
        mTN.cancel();
    }
    ```
    1. show过程：
        * 调用NMS::enqueueToast方法(参数1：包名；参数2：TN是一个IBinder.Stub，也就是远程回调；参数3：时长)
        * 调用enqueueToast先将Toast请求，封装为ToastRecord对象并添加到mToastQueue队列(实际是一个ArrayList)中
        * 对非系统应用来说，mToastQueue最多同时存在50个Record(防止DOS[Denial of Service])
            * 如果不进行DOS防范，可能会一直处理某一个应用的请求
            * 导致系统无法响应其他应用请求，这个行为就是DOS
            ```
            if (!isSystemToast) {
                int count = 0;
                final int N = mToastQueue.size();
                for (int i=0; i<N; i++) {
                    final ToastRecord r = mToastQueue.get(i);
                    if (r.pkg.equals(pkg)) {
                        count++;
                        //　如果当前队列中需要显示的toast数量,大于系统定义的最大值,则直接返回
                        if (count >= MAX_PACKAGE_NOTIFICATIONS) {
                            return;
                        }
                    }
                }
            }
            ```
        * 正常情况，一个应用不可能达到上限。Record添加到Queue中，NMS就通过showNextToastLocked方法显示当前Toast
            ```
            void showNextToastLocked() {
                ToastRecord record = mToastQueue.get(0);
                while (record != null) {
                    if (DBG) Slog.d(TAG, "Show pkg=" + record.pkg + " callback=" + record.callback);
                    try {
                        //　回调回到创建Toast时候创建的TN中
                        record.callback.show();
                        //　处理toast显示时长，在record中记录时长
                        scheduleTimeoutLocked(record);
                        return;
                    } catch (RemoteException e) {
                        ....
                    }
                }
            }
            ```
            * Toast的显示，由Record的callback完成(也就是TN对象的远程Binder)。
            * 此方法需要通过IPC完成，最终被调用的TN的方法，将会运行在在发起Toast请求的应用的Binder线程池中。
        * Toast显示完毕后，通过scheduleTimeoutLocked方法，发送一个延迟消息
            ```
            private void scheduleTimeoutLocked(ToastRecord r) {
                mHandler.removeCallbacksAndMessages(r);
                Message m = Message.obtain(mHandler, MESSAGE_TIMEOUT, r);
                //　这里无论我们设置的显示时长是多少，都只有LONG_DELAY和SHORT_DELAY这两种
                long delay = r.duration == Toast.LENGTH_LONG ? LONG_DELAY : SHORT_DELAY;
                //　延迟指定时长，发送"MESSAGE_TIMEOUT"消息,取消当前toast的显示
                mHandler.sendMessageDelayed(m, delay);
            }

            public void handleMessage(Message msg){
                case MESSAGE_TIMEOUT:
                    handleTimeout((ToastRecord)msg.obj);
                    break;
            }
            ```
        * 收到TimeOut的延迟消息之后，会处理Toast的隐藏：
            * 同样也是通过IPC，调用TN中的方法
            ```
            private void handleTimeout(ToastRecord record){
                if (DBG) Slog.d(TAG, "Timeout pkg=" + record.pkg + " callback=" + record.callback);
                synchronized (mToastQueue) {
                    //　从当前的toast队列中查找当前ToastRecord，如果有，则取消显示
                    int index = indexOfToastLocked(record.pkg, record.callback);
                    if (index >= 0) {//　取消显示当前toast
                        cancelToastLocked(index);
                    }
                }
            }
            void cancelToastLocked(int index) {
                ToastRecord record = mToastQueue.get(index);
                try {
                    //　回调到Toast$TN中的hide方法
                    record.callback.hide();
                } catch (RemoteException e) {
                }
                //　从mToastQueue队列中移除当前toast对应的ToastRecord
                mToastQueue.remove(index);
                keepProcessAliveLocked(record.pid);
                if (mToastQueue.size() > 0) {
                    // 如果还有其他的toast,继续显示其他的toast
                    showNextToastLocked();
                }
            }
            ```
        * TN::show/hide:
            ```
            public void show(IBinder windowToken) {
                if (localLOGV) Log.v(TAG, "SHOW: " + this);
                mHandler.obtainMessage(SHOW, windowToken).sendToTarget();
            }
            public void hide() {
                if (localLOGV) Log.v(TAG, "HIDE: " + this);
                mHandler.obtainMessage(HIDE).sendToTarget();
            }
            ```
            * 发送消息后，handler中调用对应方法
            * handleShow()方法，将Toast的视图添加到Window：
                ```
                mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                mWM.addView(mView, mParams);
                ```
            * handleHide()方法，移除Toast的视图
                ```
                mWM.removeViewImmediate(mView);
                ```
    2. cancel过程：
        2. TN::cancel，Toast.cancel直接调用TN::cancel方法：
           ```
           public void cancel() {
               if (localLOGV) Log.v(TAG, "CANCEL: " + this);
               mHandler.obtainMessage(CANCEL).sendToTarget();
           }

           void handleMessage(Message msg){
               ...
               case CANCEL: {
                   handleHide();
                   mNextView = null;
                   try {
                       getService().cancelToast(mPackageName, TN.this);//IPC
                   } catch (RemoteException e) { }
                   break;
               }
           }

           public void handleHide() {
               if (localLOGV) Log.v(TAG, "HANDLE HIDE: " + this + " mView=" + mView);
               if (mView != null) {
                   if (mView.getParent() != null) {
                       mWM.removeViewImmediate(mView);
                   }
                   mView = null;
               }
           }
           ```