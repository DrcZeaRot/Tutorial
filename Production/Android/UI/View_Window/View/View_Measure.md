### View的测量

measure过程要分情况：
1. 单纯的一个View：只需要通过measure方法就完成了自身的测量。
2. ViewGroup：不仅要测量自身，还需要测量子View

关于Measure：
1. View的measure是三大流程中最复杂的。
2. measure完成后，通过getMeasuredWidth/Height，可以正确获取测量宽高
3. 但某些极端情况下，系统需要多次measure才能确定最终的宽高
    * 此时，onMeasure中拿到的测量宽/高可能不准
    * 最好是在onLayout中获取View的测量宽高、最终宽高。

##### View的measure

View的测量，主要涉及到onMeasure方法：
1. onMeasure:
    ```
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        setMeasuredDimension(
            getDefaultSize(getSuggestedMinimumWidth(),widthMeasureSpec),
            getDefaultSize(getSuggestedMinimumHeight(),heightMeasureSpec),
        )
    }
    ```
2. getDefaultSize:
    ```
    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }
    ```
    1. 忽略UNSPECIFIED分支，只看AT_MOST和EXACTLY。
    2. 此方法的返回值，就是measureSpec中的SpecSize(也就是View测量后的大小)。
3. UNSPECIFIED分支，对应推荐最小宽高：
    ```
    protected int getSuggestedMinimumWidth() {
        return (mBackground == null) ? mMinWidth : max(mMinWidth, mBackground.getMinimumWidth());
    }
    ```
    * 只看一个width相关，height跟它原理相同。
    * 如果View没有设置背景(mBackground == null)，则取mMinWidth(android:minWidth属性的值，不指定默认为0)
    * 如果设置了背景，则取mBackground.getMinimumWidth()和mMinWidth的最大值。
    * Drawable::getMinimumWidth:
        ```
        public int getMinimumHeight() {
            final int intrinsicHeight = getIntrinsicHeight();
            return intrinsicHeight > 0 ? intrinsicHeight : 0;
        }
        ```
        * 尝试返回Drawable的原始宽度，没有就返回0
        * 如：ShapeDrawable无原始宽高，BitmapDrawable有原始宽高(即图片的尺寸)
4. 结论：直接继承View的自定义控件：
    1. 需要重写onMeasure方法
    2. 并且，wrap_content时的大小，需要单独指定。否则设置wrap_content就跟设置match_parent一样了
        * 因为，设置了wrap_content的View，它的SpecMode一定是AT_MOST(见[MeasureSpec](MeasureSpec.md)中表格)
        * 此时，它的宽高，等于specSize(观察上表可知，specSize就是parentSize)
        * 则设置wrap_content会导致View的宽高占满当前父容器的剩余空间。
        * 解决方案：针对需求，为wrap_content的宽/高，设置合理的数值。
            * 比如：TextView使用wrap_content会根据文字的大小调整TextView的大小
            * 可以为你的自定义View设置一个默认的宽高，示例：
                ```
                protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
                    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
                    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
                    int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
                    int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
                    int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
                    if(widthSpecMode == MeasureSpec.AT_MOST
                        && heightSpecMode == MeasureSpec.AT_MOST){
                        setMeasuredDimension(defaultWidth,defaultHeight);
                    } else if(widthSpecMode == MeasureSpec.AT_MOST){
                        setMeasuredDimension(defaultWidth,heightSpecSize);
                    } else if(heightSpecMode == MeasureSpec.AT_MOST){
                        setMeasuredDimension(widthSpecSize,defaultHeight);
                    }
                }
                ```

##### ViewGroup的measure

身为抽象类的ViewGroup，提供了measureChildren方法：
1. ViewGroup::measureChildren：进行循环measureChild
    ```
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        final int size = mChildrenCount;
        final View[] children = mChildren;
        for (int i = 0; i < size; ++i) {
            final View child = children[i];
            if ((child.mViewFlags & VISIBILITY_MASK) != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }
    ```
2. ViewGroup::measureChild
    ```
    protected void measureChild(View child, int parentWidthMeasureSpec,
            int parentHeightMeasureSpec) {
        final LayoutParams lp = child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                mPaddingLeft + mPaddingRight, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                mPaddingTop + mPaddingBottom, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
    ```
    * 获取child的LayoutParams、通过getChildMeasureSpec(结果汇总在MeasureSpec的表格中)获取子元素的MeasureSpec
    * 对各个child进行单独测量
3. 不同的ViewGroup有不同的onMeasure的实现

##### LinearLayout的measure

1. LinearLayout::onMeasure
    ```
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == VERTICAL) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }
    ```
2. 以Vertical为例：
    1. 遍历子元素，执行measureChildBeforeLayout方法，此时每个child会自行measure
    2. measure子元素过程中，通过mTotalLength存储LinearLayout垂直方向的初步高度
    3. 测量完毕子元素，LinearLayout再测量自己的大小
        1. 如果采用match_parent或具体数值，则测量过程跟View一样
        2. 如果采用wrap_content，则其高度是所有子元素高度的和(但不能超过父容器的剩余)

##### 其他

思考题：想在Activity启动时，获取某个View的宽高
* 注意：
    1. View的measure过程，与Activity的生命周期是不同步的
    2. onCreate、onStart、onResume中，View可能还没有测量完毕、无法获得正确的宽高
* 你需要以下4种方法：
    1. Activity/View::onWindowFocusChanged
        * 典型使用：
            ```
            public void onWindowFocusChanged(boolean hasFocus){
                super.onWindowFocusChanged(hasFocus);
                if(hasFocus){
                    int width = view.getMeasuredWidth();
                    int height = view.getMeasuredHeight();
                }
            }
            ```
        * 此方法含义：View已经初始化完毕了，宽高已经准备好了，此时去获取没有问题
        * 但要注意的是：onWindowFocusChanged会被调用多次->Activity窗口获取/失去焦点都会被调用。
        * 频繁执行onResume、onPause时，此方法会被频繁调用
    2. view.post(runnable):
        * 典型使用：
            ```
            protected void onStart(){
                super.onStart();
                view.post(() -> {
                    int width = view.getMeasuredWidth();
                    int height = view.getMeasuredHeight();
                });
            }
            ```
        * 通过post，可以将一个runnable添加到消息队列尾部
        * 等待Looper调用此Runnable时，View也已经初始化好了。
    3. ViewTreeObserver:
        * 典型使用：
            ```
            protected void onStart(){
                super.onStart();
                ViewTreeObserver observer = view.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(() -> {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    int width = view.getMeasuredWidth();
                    int height = view.getMeasuredHeight();
                });
            }
            ```
        * 当View树的状态发生改变，或View树内部的View的可见性发生改变时，OnGlobalLayoutListener:onGlobalLayout将回调
        * 注意：随着View树的状态改变，onGlobalLayout会被回调多次。
    4. view.measure(widthMeasureSpec,heightMeasureSpec)
        * 手动调用View的measure方法，使View完成measure
        * 分如下情况
            1. match_parent
                * 直接放弃，无法测量出具体的宽/高
                * 原因简单：构造所需的MeasureSpec，需要parentSize，但我们无法知道
            2. 具体数值
                * 加入宽高都是100px
                    ```
                    int widthMeasureSpec =
                        MeasureSpec.makeMeasureSpec(100,MeasureSpec.EXACTLY);
                    int heightMeasureSpec =
                        MeasureSpec.makeMeasureSpec(100,MeasureSpec.EXACTLY);
                    view.measure(widthMeasureSpec,heightMeasureSpec);
                    ```
            3. wrap_content
                ```
                int widthMeasureSpec =
                    MeasureSpec.makeMeasureSpec((1 << 30) - 1,MeasureSpec.AT_MOST);
                int heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec((1 << 30) - 1,MeasureSpec.AT_MOST);
                view.measure(widthMeasureSpec,heightMeasureSpec);
                ```
                * SpecSize是MeasureSpec的低30位，使用(1 << 30) - 1，就是SpecSize的最大值。
        * 如下2种是错误的：
            1. ```
                int widthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(-1,MeasureSpec.EXACTLY);
                int heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(-1,MeasureSpec.EXACTLY);
                view.measure(widthMeasureSpec,heightMeasureSpec);
               ```
           2. view.measure(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
           3. 以上两种，都是不合法的MeasureSpec，也就不可能拿到合法的SpecMode和SpecSize。