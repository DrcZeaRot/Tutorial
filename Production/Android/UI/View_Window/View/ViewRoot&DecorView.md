### ViewRoot和DecorView

```
ActivityThread中，Activity对象被创建完毕后，
会将DecorView添加到Window中，同时创建ViewRootImpl对象。
并将ViewRootImpl对象和DecorView建立关联如下：

root = new ViewRootImpl(view.getContext(),display);
root.setView(view,wparams,panelParentView);
```

View的绘制流程，从ViewRoot的performTraversals开始：
1. ViewRootImpl::performTraversals内部调用如下3个方法
    * ViewRootImpl:performMeasure
        * mView.measure -> View::onMeasure
    * ViewRootImpl:performLayout
        * mView.layout -> View::onLayout
    * ViewRootImpl:performDraw
        * drawSoftWare -> mView.draw -> View::onDraw
2. View::onMeasure：对所有子元素进行遍历measure
3. View::onLayout：对所有子元素进行遍历layout
4. View::draw：dispatchDraw对所有子元素进行遍历draw