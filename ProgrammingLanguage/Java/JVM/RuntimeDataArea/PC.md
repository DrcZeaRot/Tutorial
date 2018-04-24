### 程序计数器(Program Counter Register)

* 程序计数器(Program Counter Register)是一块较小的内存空间，它可以看作是[当前线程所执行的字节码的行号指示器]()。

* 在虚拟机的概念模型里(仅是概念模型，各种虚拟机可能通过一些更高效的方式去实现)，字节码解释器工作时，就是通过改变这个计数器的值，来选取吓一跳需要执行的字节码指令。分支、循环、跳转、异常处理、线程恢复等基础功能，都需要一来这个计数器来完成。


##### 多线程相关

* 由于JVM的多线程是通过[线程轮流切换并分配处理器执行时间]()的方式来实现的，在任何一个确定的时刻，一个处理器(对于多核处理器来说是一个内核)都只会执行一条线程中的指令。
* 因此，为了线程切换后能恢复到正确的执行为止，每条线程都需要一个独立的程序计数器，各条线程之间的计数器互不影响，独立存储。
* 我们称这类内存区域为"线程私有"的内存

##### 其他、OOM

* 如果线程正在执行的是一个Java方法，这个计数器记录的是正在执行的虚拟机字节码指令的地址
* 如果正在执行的是Native方法，这个计数器则为空(Undefined)。
* 此内存区域是唯一一个在JVM规范中没有规定任何OutOfMemoryError情况的区域。