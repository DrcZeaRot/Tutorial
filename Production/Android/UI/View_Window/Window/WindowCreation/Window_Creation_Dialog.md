### Dialog的Window创建

流程与Activity类似：
1. 创建Window，位于构造函数中：
    ```
    Dialog(@NonNull Context context, @StyleRes int themeResId, boolean createContextThemeWrapper) {
        if (createContextThemeWrapper) {
            if (themeResId == ResourceId.ID_NULL) {
                final TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(R.attr.dialogTheme, outValue, true);
                themeResId = outValue.resourceId;
            }
            mContext = new ContextThemeWrapper(context, themeResId);
        } else {
            mContext = context;
        }

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        final Window w = new PhoneWindow(mContext);
        mWindow = w;
        w.setCallback(this);
        w.setOnWindowDismissedCallback(this);
        w.setOnWindowSwipeDismissedCallback(() -> {
            if (mCancelable) {
                cancel();
            }
        });
        w.setWindowManager(mWindowManager, null, null);
        w.setGravity(Gravity.CENTER);

        mListenersHandler = new ListenersHandler(this);
    }
    ```
    * 依旧是创建了一个PhoneWindow实例
2. 初始化DecorView、添加Dialog视图到DecorView中
    ```
    public void setContentView(@LayoutRes int layoutResID) {
            mWindow.setContentView(layoutResID);
        }
    ```
    * 此处与Activity相同，PhoneWindow的setContentView方法负责处理
3. 添加DecorView到Window中，在show方法中处理：
    ```
    public void show() {
        ...
        mDecor = mWindow.getDecorView();
        ...
        mWindowManager.addView(mDecor, l);
    }
    ```
    * PhoneWindow::getDecorView：
        ```
        public final View getDecorView() {
            if (mDecor == null || mForceDecorInstall) {
                installDecor();
            }
            return mDecor;
        }
        ```
    * dismissDialog方法中，会从WindowManager中移除Dialog的DecorView。
        * 此处使用：mWindowManager.removeViewImmediate(mDecor);

#### Dialog的Context
* 普通Dialog必须使用Activity的Context，如果使用Application的Context会抛出BadTokenException
    * 上述异常，由于没有<应用token>导致
    * <应用token>一般只有Activity拥有
* 但系统Window比较特殊，可以不需要token。
    * 指定Dialog的Window类型为系统Window，也可以正常弹出Dialog
    * 系统Window的层级范围2000~2999
    * 可以如下使用：
        ```
        dialog.getWindow().setType(LayoutParams.TYPE_SYSTEM_ERROR)，
        同时添加权限：<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
        ```