### Window内部机制

Window是一个抽象的概念：
* Window并不实际存在：
    * 每个Window都对应一个View和一个ViewRootImpl
    * Window和View通过ViewRootImpl建立联系
    * 因此Window并不是实际存在的，它是以View的形式存在的
* 从WindowManager定义中也可看出：
    * 提供的3个接口方法都是针对View的
    * 这说明，View才是Window存在的尸体
* 实际使用中，无法直接访问Window
* 对Window的访问，必须通过WindowManager


对Window的操作：
* [Window的添加过程](WindowInnerMecanism/Window_InnerMecanism_Add.md)
* [Window的删除过程](WindowInnerMecanism/Window_InnerMecanism_Remove.md)
* [Window的更新过程](WindowInnerMecanism/Window_InnerMecanism_Update.md)