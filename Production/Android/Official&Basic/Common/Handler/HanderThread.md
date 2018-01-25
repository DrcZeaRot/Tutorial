## HandlerThread

### HandlerThread特点
* HandlerThread将loop转到子线程中处理，说白了就是将分担MainLooper的工作量，降低了主线程的压力，使主界面更流畅。
* HandlerThread拥有自己的消息队列，它不会干扰或阻塞UI线程。
* 开启一个线程起到多个线程的作用。处理任务是串行执行，按消息发送顺序进行处理。HandlerThread本质是一个线程，在线程内部，代码是串行处理的。
* 但是由于每一个任务都将以队列的方式逐个被执行到，一旦队列中有某个任务执行时间过长，那么就会导致后续的任务都会被延迟处理。
* HandlerThread拥有自己的消息队列，它不会干扰或阻塞UI线程。
* 对于网络IO操作，HandlerThread并不适合，因为它只有一个线程，还得排队一个一个等着。

[HandlerThread常见使用](HandlerSample.md)