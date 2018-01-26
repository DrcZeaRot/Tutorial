## LocalBroadcastManager 本地广播

基础用法：

获取本地广播管理者：
>val manager = LocalBroadcastManager.getInstance(context)

注册广播
>manager.registerReceiver(receiver,filter)

发送本地广播
>manager.sendBroadcast(intent)

解除注册广播
>manager.unregisterReceiver(receiver)


本地广播的优势：
* 因广播数据在本应用范围内传播，你不用担心隐私数据泄露的问题。
* 不用担心别的应用伪造广播，造成安全隐患。
* 相比在系统内发送全局广播，它更高效。

简单解析:
* register本地广播时 将Receiver跟IntentFilter实例包装、缓存到LocalBroadcastManager的Map中
* 发送广播不再经过IPC、Binder机制，直接回调Receiver的onReceive方法，实际上已经基本简化为回调