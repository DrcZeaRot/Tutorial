### IPC

```
IPC：Inter-Process-Communication
含义为：进程间通信/跨进程通信，指两个进程之间进行数据交换的过程。
```


* IPC不是Android独有的：任何一个操作系统，都需要相应的IPC机制：
    1. Window可以通过剪贴板、管道和邮槽等进行IPC
    2. Linux可以通过命名管道、共享内存、信号量等进行IPC
* 对于Android：
    1. Android是一种基于Linux内核的移动操作系统
        * 它的IPC方式没有完全继承自Linux
    2. Android中最有特色的IPC机制就是Binder
    3. 除了Binder，Android还支持Socket通信


#### 具体章节

1. [Android中的多进程模式](Process/MultiProcessInAndroid.md)
2. IPC基础概念介绍
    1. [Serializable&Parcelable](IPC_Basic/Serializable&Parcelable.md)
    2. [Binder](Binder/Binder_Intro.md)
3. Android中的IPC方式
    1. Bundle：四大组件通过Intent通信，Intent可以携带Bundle。Fragment也可以通过Bundle通信。
    2. 文件共享：LocalFile在没有并发问题时，也是一种比较理想的通信方式。
    3. Messenger：基础的AIDL/Handler的封装。
    4. AIDL：功能最全面的IPC
    5. ContentProvider：SQL的封装
    6. Socket：最基础的网络通信。