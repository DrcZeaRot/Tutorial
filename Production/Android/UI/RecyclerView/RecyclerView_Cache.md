### RecyclerView缓存机制，与ListView对比浅析

```
ListView与RecyclerView缓存机制原理大致相似，
离屏的ItemView即被回收至缓存，入屏的ItemView则会优先从缓存中获取，只是ListView与RecyclerView的实现细节有差异
```

#### 缓存机制对比

1. 层级不同：
    ```
    RecyclerView比ListView多两级缓存，支持多个离ItemView缓存，
    支持开发者自定义缓存处理逻辑，支持所有RecyclerView共用同一个RecyclerViewPool(缓存池)。
    ```
    1. ListView：2级缓存

        |成员|是否回调onCreateView|是否回调bindView|生命周期|备注|
        |:-:|:-:|:-:|:-:|:-:|
        |mActiveViews|否|否|onLayout函数周期|用于屏幕内ItemView快速重用|
        |mScrapViews|否|是|mAdapter更换时清空|-|

    2. RecyclerView：4级缓存

        |成员|是否回调onCreateView|是否回调bindView|生命周期|备注|
        |:-:|:-:|:-:|:-:|:-:|
        |mAttachedScrap|否|否|onLayout函数周期|用于屏幕内ItemView快速重用|
        |mCacheViews|否|否|mAdapter更换缓存到mRecyclerPool|默认上限2，缓存屏幕外2个|
        |mViewCacheExtension|-|-|-|不直接使用，需要用户实现|
        |mRecyclerPool|否|是|adapter更换时清空|默认上限5，可实现为所有公用一个|
    * 对比：
        1. mActiveViews和mAttachedScrap功能类似：用于快速重用可见ItemView，不需要重新create和bind
        2. mScrapView和mCachedViews+mRecyclerViewPool功能相似：用于缓存离开屏幕的View
        3. RecyclerView的优势：
            1. mCacheViews可以做到屏幕外的重用不需要bindView
            2. mRecyclerPool可以供多个RecyclerView使用(如ViewPager+多个列表有优势)
2. 缓存不同
    1. RecyclerView缓存了RecyclerView.ViewHolder
        * 抽象理解为：View+ViewHolder(避免每次createView时findView)+flag(状态标识，局部刷新用)
    2. ListView缓存View

#### 缓存获取过程

[ListView过程图](img/ListView_Caching.webp)

[RecyclerView过程图](img/RecyclerView_Caching.webp)

区别：
1. 屏幕外缓存的处理不同
    1. RecyclerView中mCacheViews获取屏幕外缓存的ViewHolder，可以直接使用ViewHolder中的View，无需重新bindView
    2. ListView从mScrapViews中获取缓存的View，但没有直接使用View，而是传递给getView方法，一定会重新bindView
2. position与缓存的映射不同
    1. ListView是pos->View
    2. RecyclerView是pos->(view,viewHolder,flag)
        * 通过flag可以判断是否需要重新bindView，实现局部刷新就靠它了

#### 局部刷新

```
以RecyclerView中notifyItemRemoved(1)为例，最终会调用requestLayout()，需要重新绘制：onMeasure()→onLayout()→onDraw()
```

其中，onLayout()为重点，分为三步：
1. dispatchLayoutStep1()：记录RecyclerView刷新前列表项ItemView的各种信息，如Top,Left,Bottom,Right，用于动画的相关计算；
2. dispatchLayoutStep2()：真正测量布局大小，位置，核心函数为layoutChildren()；
    * [layoutChildren](img/RecyclerView_layoutChildren.webp)，调用Recycler.fill
        * [notifyItemRemoved与flag](img/RecyclerView_flag.webp)
    * [Recycler.fill](img/Recycler_fill.webp)
        ```
        当调用fill()中RecyclerView.getViewForPosition(pos)时，
        RecyclerView通过对pos和flag的预处理，使得bindview只调用一次.
        ```
3. dispatchLayoutStep3()：计算布局前后各个ItemView的状态，如Remove，Add，Move，Update等，如有必要执行相应的动画.

#### 总结

ListView和RecyclerView最大的区别在于数据源改变时的缓存的处理逻辑：
1. ListView是”一锅端”，将所有的mActiveViews都移入了二级缓存mScrapViews
2. 而RecyclerView则是更加灵活地对每个View修改标志位，区分是否重新bindView



##### 参考

[Android ListView 与 RecyclerView 对比浅析--缓存机制 by 腾信Bugly](https://mp.weixin.qq.com/s?__biz=MzA3NTYzODYzMg==&mid=2653578065&idx=2&sn=25e64a8bb7b5934cf0ce2e49549a80d6&chksm=84b3b156b3c43840061c28869671da915a25cf3be54891f040a3532e1bb17f9d32e244b79e3f&scene=21#wechat_redirect)