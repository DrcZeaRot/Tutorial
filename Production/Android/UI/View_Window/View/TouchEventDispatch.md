### View事件分发

##### 有关方法

1. 分发事件(ViewGroup、View都可以分发事件)：
    ```
    public boolean dispatchTouchEvent(MotionEvent event)
    ```
    * 如果事件能够传递给一个View，则此方法一定会被调用
    * 返回值受此View的onTouchEvent和下一级View的dispatchTouchEvent影响。表示是否消耗当前事件
2. 拦截事件(只有ViewGroup可以拦截事件)：
    ```
    public boolean onInterceptTouchEvent(MotionEvent event)
    ```
    * 在dispatchTouchEvent方法内部调用
    * 用于判断是否拦截事件
    * 如果某个View拦截了此事件，则在同一个事件序列中，此方法不会再次被调用
    * 返回结果表示是否拦截当前事件
3. 处理事件(当然，这3个方法都可以处理事件)：
    ```
    public boolean onTouchEvent(MotionEvent event)
    ```
    * dispatchTouchEvent方法中调用，用于处理点击事件
    * 返回结果表示：是否消耗当前事件
    * 如果不消耗，则在同一个事件序列中，此View无法再次接受到事件

三者关系的伪代码如下：
```
public boolean dispatchTouchEvent(MotionEvent ev){
    boolean consume = false;
    if(onInterceptTouchEvent(ev)){
        consume = onTouchEvent(ev);
    } else {
        consume = child.dispatchTouchEvent(ev);
    }
    return consume;
}
```

##### 流程简述

一个事件产生后，传递过程遵循：Activity->Window->View：
1. 事件先传递给ViewGroup
    * 首先调用ViewGroup::dispatchTouchEvent，然后看ViewGroup是否拦截此事件
    * 如果onInterceptTouchEvent返回true
        * 表示它要拦截事件，事件传给onTouchEvent处理
    * 如果onInterceptTouchEvent返回false
        * 表示它不拦截事件，事件传给它的子元素的dispatchTouchEvent.
2. 事件传递给View之后
    * 如果它设置了OnTouchListener，则要看Listener::onTouch方法的返回
        * 返回true，表示事件已处理完毕，onTouchEvent方法就不会被调用
        * 返回false，表示事件没处理完毕，onTouchEvent方法还会被调用
    * 在onTouchEvent方法中，如果View设置了OnClickListener
        * 则Listener::onClick方法会被调用
    * OnTouchListener > onTouchEvent > OnClickListener
3. 如果遍历所有子元素，事件都没有被合适地处理：
    * 有两种情况：
        1. ViewGroup没有子元素
        2. 子元素的dispatchTouchEvent返回了false(通常是onTouchEvent返回false)
    * 此时，当前ViewGroup会自己处理本次事件
4. 当所有Activity中的View、ViewGroup都没有对事件进行合适处理：
    * 事件最终分发到Activity::onTouchEvent中。

##### 11条结论

观察源码，有如下结论：
1. 同一个时间序列：
    * 从手指接触屏幕开始，到手指离开屏幕的过程中，产生的一系列事件
    * 这个时间序列以DOWN事件开始，中间还有若干个MOVE事件，最终以UP事件结束
2. 正常情况下：一个事件序列只能被一个View拦截且消耗
    * 所谓"正常情况"需要参考第3条，拦截就对整个事件序列获得了唯一指定处理权
    * 但可以通过特殊手段让其他的View也处理这个事件(比如：通过onTouchEvent强行传递给其他View)
3. 某个View一旦决定拦截事件，那这个事件序列都只能由它来处理，并且它的onInterceptTouchEvent不会再次被调用。
4. 某个View一旦开始处理事件：
    * 如果它不消耗ACTION_DOWN(onTouchEvent中返回false)，那同一事件序列中的其他事件，都不会再交给它处理
    * 并且，事件将重新交给它的父元素去处理(父元素的onTouchEvent会被调用)
5. 如果View不消耗除了ACTION_DOWN外的其他事件,则这个点击事件会消失
    * 此时，父元素的onTouchEvent不会被调用
    * 当前View也可以持续收到后续的时间
    * 最终，这些消失的事件将被传递给Activity处理
6. ViewGroup默认不拦截任何事件(ViewGroup的onInterceptTouchEvent默认返回false)
7. View没有onInterceptTouchEvent方法，一旦有时间传递给它，onTouchEvent方法就会被调用
8. View的onTouchEvent默认都会讲事件消耗(返回true)，除非它不可点击(clickable、longClickable同时false)
    * longClickable默认都是false
    * clickable默认比较符合预期：TextView为false、Button为true
9. View的enable属性不影响onTouchEvent的默认返回值
    * 即使一个disable的View，clickable/longClickable某一个为true，则onTouchEvent就返回true
10. onClick会发生的前提是：当前View是clickable的、并且它收到了DOWN和UP事件
11. 事件传递是从外向内的：
    * 事件总是先传递给父元素、再由父元素分发给子View
    * 除了ACTION_DOWN事件，getParent().requestDisallowInterceptTouchEvent()方法可以在子元素中对父元素的事件分发进行干预。

##### 源码简述

1. Activity::dispatchTouchEvent
    ```
    public boolean dispatchTouchEvent(MotionEvent ev){
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            //默认是一个空方法，一般是锁屏界面可能会特殊处理的
            onUserInteraction();
        }
        //分发给Window(PhoneWindow)，内部还会分发给ViewGroup和子View
        if(getWindow().superDispatchTouchEvent(ev)){
            return true;
        }
        //如果所有的ViewGroup、View都不处理，上方的if(..)不会走，最终回到Activity::onTouchEvent
        return onTouchEvent(ev);
    }
    ```
2. PhoneWindow::superDispatchTouchEvent
    ```
    public boolean superDispatchTouchEvent(MotionEvent ev){
        //Window将事件传递给DecorView，DecorView是一个ViewGroup
        //此方法直接调ViewGroup::dispatchTouchEvent
        return mDecore.superDispatchTouchEvent(ev);
    }
    ```
3. ViewGroup::dispatchTouchEvent

##### 参考

[Android开发艺术探索-3.4]()