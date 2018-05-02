### 自定义View

##### 自定义View的分类

1. 继承View，重写onDraw方法
    * 主要用于实现一些不规则的效果
    * 即：这种效果不方便通过布局的组合方式来达到
    * 往往需要静态或动态地显示一些不规则的图形
    * 需要自己支持wrap_content、padding也需要自行处理
2. 继承ViewGroup，派生特殊的onLayout
    * 主要用于实现自定义的布局
    * 需要合适地处理ViewGroup的测量、布局两个过程
    * 同时需要处理子元素的测量、布局过程
3. 继承特定的View(如TextView)
    * 一般用于扩展已存在的View的某些功能
    * 比较容易实现，不需要处理wrap_content和padding
4. 继承特定的ViewGroup(如LinearLayout)
    * 处理某一个View组合在一起的效果
    * 不需要额外处理
    * 与2的区别：2更倾向于View的底层

##### 自定义View的须知

1. 让View支持wrap_content
2. 如果有必要，让View支持padding
3. 尽量不要在View中使用Handler
    * 除非必须要是用Handler发消息，否则应该使用postXXX()方法
4. View中如果有线程/动画，要及时停止，onDetachedFromWindow是一个好的时机
    * 包含此View的Activity退出、或当前View被remove时，回调此方法
    * 与之相对的是onAttachedToWindow
5. View带有滑动嵌套时，处理好滑动冲突