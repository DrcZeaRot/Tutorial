### system_server发起请求


0. 比如，从Launcher点击一个图标，此时目标App的进程不存在，需要先创建进程
    * 从Launcher3这个Activity，前往目标Activity
    * Activity的创建流程见[Activity工作过程](../../ComponentWorkFlow/ActivityWorkFlow.md)：
        * 流程走到结论的2.3
        1. ActivityStackSupervisor::startSpecificActivityLocked调用AMS::startProcessLocked
        2. 接下来就调用到Process::start
        3. 按照接下来的进程子创建流程，最终会走到ActivityThread::main
1. 入口方法：Process:start，桥接到ZygoteProcess::start
2. 跳转ZygoteProcess::startViaZygote
    * 此过程生成argsForZygote列表
    * 其中保存了：进程uid、gid、groups、target-sdk、nice-name等参数
    * openZygoteSocketIfNeeded尝试获取一个ZygoteState
    * 发送ZygoteState和参数列表给zygoteSendArgsAndGetResult方法
3. ZygoteProcess::openZygoteSocketIfNeeded
    * 此方法向主Zygote发起connect请求
    * 主Zygote匹配不成功，则采用第二个Zygote发送connect
    * 这里的connect会触发[Zygote进程](ZygoteProcess.md)中ZygoteInit::runSelectLoop的客户端请求。
    * 主要是根据当前的abi来选择，与zygote/zygote64哪一个来通信。
4. ZygoteProcess::zygoteSendArgsAndGetResult，主要功能：
    * 通过socket通道向[Zygote进程](ZygoteProcess.md)发送<方法2>中的参数列表
    * 然后进入阻塞等待状态，直到远端socket服务端，返回新创建的进程pid，才能返回。
    * 关于阻塞等待时长：Google考虑是否应该有TimeOut，但至少API26还没有。