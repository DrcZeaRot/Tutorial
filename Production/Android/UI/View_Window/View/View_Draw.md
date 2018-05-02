### View的绘制

View的绘制过程遵循如下几步：
1. 绘制背景：background.draw(canvas)
2. 绘制自己：onDraw
3. 绘制children：dispatchDraw
    * 遍历调用所有子View的draw方法
4. 绘制装饰：onDrawScrollBars

特殊方法：setWillNotDraw
* 代码：
    ```
    public void setWillNotDraw(boolean willNotDraw) {
        setFlags(willNotDraw ? WILL_NOT_DRAW : 0, DRAW_MASK);
    }
    ```
* 如果一个View不需要绘制任何内容，设置这个为true，系统会进行相应优化
* 默认View没有开启这个标记，但ViewGroup默认开启