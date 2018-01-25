### 以Android消息体系中Looper.cpp中代码为例


    Looper::Looper(bool allowNonCallbacks) :
            mAllowNonCallbacks(allowNonCallbacks), mSendingMessage(false),
            mResponseIndex(0), mNextMessageUptime(LLONG_MAX) {
        int wakeFds[2];
        int result = pipe(wakeFds);
        LOG_ALWAYS_FATAL_IF(result != 0, "Could not create wake pipe.  errno=%d", errno);

        mWakeReadPipeFd = wakeFds[0];
        mWakeWritePipeFd = wakeFds[1];

        result = fcntl(mWakeReadPipeFd, F_SETFL, O_NONBLOCK);
        LOG_ALWAYS_FATAL_IF(result != 0, "Could not make wake read pipe non-blocking.  errno=%d",
                errno);

        result = fcntl(mWakeWritePipeFd, F_SETFL, O_NONBLOCK);
        LOG_ALWAYS_FATAL_IF(result != 0, "Could not make wake write pipe non-blocking.  errno=%d",
                errno);

        mIdling = false;

        // Allocate the epoll instance and register the wake pipe.
        mEpollFd = epoll_create(EPOLL_SIZE_HINT);
        LOG_ALWAYS_FATAL_IF(mEpollFd < 0, "Could not create epoll instance.  errno=%d", errno);

        struct epoll_event eventItem;
        memset(& eventItem, 0, sizeof(epoll_event)); // zero out unused members of data field union
        eventItem.events = EPOLLIN;
        eventItem.data.fd = mWakeReadPipeFd;
        result = epoll_ctl(mEpollFd, EPOLL_CTL_ADD, mWakeReadPipeFd, & eventItem);
        LOG_ALWAYS_FATAL_IF(result != 0, "Could not add wake read pipe to epoll instance.  errno=%d",
                errno);
    }

##### 概念简介
1. 关于管道，简单来说，管道就是一个文件。
2. 在管道的两端，分别是两个打开文件文件描述符，这两个打开文件描述符都是对应同一个文件，其中一个是用来读的，别一个是用来写的。
3. 一般的使用方式就是，一个线程通过读文件描述符中来读管道的内容，当管道没有内容时，这个线程就会进入等待状态，而另外一个线程通过写文件描述符来向管道中写入内容，写入内容的时候，如果另一端正有线程正在等待管道中的内容，那么这个线程就会被唤醒。
4. 这个等待和唤醒的操作是如何进行的呢，这就要借助Linux系统中的epoll机制了。 Linux系统中的epoll机制为处理大批量句柄而作了改进的poll，是Linux下多路复用IO接口select/poll的增强版本，它能显著减少程序在大量并发连接中只有少量活跃的情况下的系统CPU利用率。

##### 示例代码解析
1. pipe(wakeFds)，该函数创建了两个管道句柄。
2. mWakeReadPipeFd=wakeFds[0]，是读管道的句柄。
3. mWakeWritePipeFd=wakeFds[1]，是写管道的句柄。
4. epoll_create(EPOLL_SIZE_HINT)是创建epoll句柄。
5. epoll_ctl(mEpollFd, EPOLL_CTL_ADD, mWakeReadPipeFd, & eventItem)，它的作用是告诉mEpollFd，它要监控mWakeReadPipeFd文件描述符的EPOLLIN事件，即当管道中有内容可读时，就唤醒当前正在等待管道中的内容的线程。