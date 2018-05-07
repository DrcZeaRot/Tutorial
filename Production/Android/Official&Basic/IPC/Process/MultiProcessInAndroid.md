### Android中的多进程

##### 开启多进程

```
正常情况下，Android的多进程指：同一个App中存在多个进程。
两个、多个应用之间的多进程此处不予讨论。
```

Android中，使用多进程只有一种方法：
* 给四大组件在AndroidManifest中指定"android:process"属性，除此之外别无他法
* 我们无法给一个线程、或一个实体类，指定其运行时所在的线程
* 其实还有一种非常规的多进程方法：通过JNI在native层fork一个新的进程(特殊情况，不常用)。
* 示例如下，假设包名为"com.example"：
    ```
    <activity
    android.name="xxx.xxx.MainActivity"/>
    <activity
    android.name="xxx.xxx.Activity1"
    android:process=":remote"/>
    <activity
    android.name="xxx.xxx.Activity2"
    android:process="com.example.remote"/>
    ```
    * 此时系统多创建两个新进程：
        * Activity1的process名为"com.example:remote"
        * Activity2的process名为"com.example.remote"
        * MainActivity运行在默认继承中，process名为"com.example"
    * 解释一下":"开头和具体名称的区别：
        1. 以":"开头的进程，属于当前应用的私有进程。其他应用的组件不可以跑在这个进程里
        2. 不以":"开头的进程属于全局进程，其他应用通过ShareUID方式，可以跑在这个进程里
            * 注：两个应用通过ShareUID跑在一个进程中有额外需求：
            * 需要两个应用有相同的ShareUID，并且签名相同才可以
            * 这种情况下，他们可以互相访问对方的私有数据：data目录、组件信息等(不管是否跑在一个进程中)
            * 如果真的跑在一个进程中，他们还可以额外共享：内存数据。此时，它们更像一个应用的两个部分。

##### 多进程模式的运行机制

> 应用开启多进程之后，各种奇怪的现象都出现了

* 一般，多进程有如下问题：
    1. 静态成员、单例模式，完全失效
    2. 线程同步机制，完全失效
    3. SharedPreference可靠性下降
    4. Application多次创建
* 上述问题原因：
    1. 前2个问题，都与虚拟机相关
        * Android为每个应用分配了一个独立的虚拟机(实际是，为每个进程分配一个独立的虚拟机)
        * 进程不同了，导致访问同一个类对象，在不同虚拟机中有多份副本。
    2. SP底层通过XML的读写实现，并发显然会出现IO问题。
    3. 在新进程中，需要分配新的虚拟机，实际上是启动App的一个过程。
