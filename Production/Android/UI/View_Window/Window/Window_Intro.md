### Window简介

* Window表示一个窗口的概念
    ```
    某些特殊时候，需要在桌面上显示一个类似悬浮窗的东西，就需要Window来实现这个效果
    ```
* Window是一个抽象类，唯一的具体实现是PhoneWindow
* 使用WindowManager创建一个Window。
    * WindowManager是外界访问Window的入口
    * Window的具体实现，位于WindowManagerService中
    * WindowManager和WindowManagerService的交互是一个IPC过程
* Android中所有的视图，都是通过Window来呈现的
    * Activity、Dialog、Toast的视图，都是附加在Window上的
    * 实际上，Window是View的直接管理者
    * View的事件分发机制中，时间由Window传递给DecorView，再由DecorView传递给具体的View
    * Activity::setContentView，底层也是通过Window来完成

##### Window和WindowManager

如下代码演示通过WindowManager添加Window的过程：
```
//in an activity

mFloatingButton = new Button(this);
mFloatingButton.setText("button");
mLayoutParams = new WindowManager.LayoutParams(
    LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,
    type: 0 , flags: 0 , format: PixelFormat.TRANSPARENT
);
mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
    |LayoutParams.FLAG_NOT_FOCUSABLE
    |LayoutParams.FLAG_SHOW_WHEN_LOCKED;
mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
mLayoutParams.x = 100;
mLayoutParams.y = 300;
mWindowManager.addView(mFloatingButton,mLayoutParams);
```
* 上述代码将一个Button添加到屏幕坐标(100,300)的位置
* 关键参数：
    1. flags：表示Window的属性，有很多选项，通过这些选项控制Window的显示特性，如：
        1. FLAG_NOT_FOCUSABLE:Window不需要获取焦点，也不需要接收各种输入事件
            * 此标记同时启用FLAG_NOT_TOUCH_MODAL，最终事件会直接传递给下层的具有焦点的Window
        2. FLAG_NOT_TOUCH_MODAL：
            * 此模式下，系统会将当前Window区域以外的事件传递给底层的Window
            * 当前Window区域以内的事件则自己处理
            * 一般要开启此标记，否则其他Window无法收到点击事件
        3. FLAG_SHOW_WHEN_LOCKED：开启可以让Window显示在锁屏界面
    2. type：表示Window类型
        * Window有3种类型：
            1. 应用Window：对应一个Activity
            2. 子Window：不能单独存在，需要附属在特定的Window中(如一些常见的Dialog)
            3. 系统Window：需要声明权限，才能创建的Window(如Toast、系统状态栏)
* Window是分层的：
    * 每个Window都有对应的z-ordered，层级大的会覆盖在层级小的Window上(与HTML中z-index概念一致)
    * 上述3类Window中：
        1. 应用Window的层级范围：1~99
        2. 子Window的层级范围：1000~1999
        3. 系统Window的层级范围：2000~2999
    * 这些层级对应WindowManager.LayoutParams的type参数
    * 想要Window处于所有Window的最顶层，采用更大的层级即可
        * 一般直接选用系统层级的常量如：TYPE_SYSTEM_OVERLAY或TYPE_SYSTEM_ERROR
        * 如：mLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR
        * 注意：需要声明对应权限，如上述type对应权限：<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>

##### 常用方法

WindowManager中常用的只有从ViewManager中继承的3个方法：
```
public interface ViewManager{
    void addView(View view, ViewGroup.LayoutParams params);
    void updateViewLayout(View view, ViewGroup.LayoutParams params);
    void removeView(View view);
}
```

WindowManager对Window的操作过程，更像是在操作Window中的View。