### View的布局

* Layout的作用是：ViewGroup来确定子元素的位置
* 当ViewGroup的位置被确定后，它在onLayout中遍历所有子元素、调用其layout方法
* 子元素的layout方法中，又调用子元素的onLayout。
* layout方法确定View本身的位置，onLayout方法确定所有子元素的位置。

##### layout简单流程

1. 首先通过setFrame方法来设定View的4个顶点的位置：
    * 也就是初始化mLeft、mRight、mTop和mBottom这4个值。
    * View的4个顶点一旦确定，则View在父容器中的位置也就确定了
2. 接着调用onLayout方法：
    * 用途是：父容器确定子元素的位置
    * 不同的View、ViewGroup的onLayout实现都可能不同
3. LinearLayout::onLayout
    ```
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mOrientation == VERTICAL) {
            layoutVertical(l, t, r, b);
        } else {
            layoutHorizontal(l, t, r, b);
        }
    }
    ```
    * 主要分析layoutVertical
    * 遍历所有子元素，调用setChildFrame为子元素指定位置(调用子元素的layout)
    * childTop逐渐增大，也就是说：后面的子元素会被放置在靠下的位置
4. 父元素在layout方法中完成自己的定位、再通过onLayout调用子元素的layout、子元素通过自己的layout定位自己，一层一层完成View树的layout
    * setFrame中，对对4个顶点进行赋值。
    * 对比getMeasuredWidth和getWidth：
        ```
        public final int getMeasuredWidth() {
            return mMeasuredWidth & MEASURED_SIZE_MASK;
        }
        public final int getWidth() {
            return mRight - mLeft;
        }
        ```
        * getMeasuredWidth形成于measure过程
        * getWidth形成于layout过程
        * 通常，测量宽高与最终宽高可以认为是相同的
        * 但某些特殊情况的确会导致两者不同。