## 动态广播

对称使用注册/解除注册，防止动态广播内存泄漏

    lateinit var receiver: BroadcastReceiver
    lateinit var filter: IntentFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(receiver, filter)
    }

    @OnClick(R.id.btnReceiver)
    fun onSthClicked(){
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }




##### 常驻广播StickyBroadcast(API23开始过时)

需求权限

    <uses-permission android:name="android.permission.BROADCAST_STICKY"/>
    context.sendStickyBroadcast(intent)
    context.sendStickyOrderedBroadcast(intent,...)
    context.removeStickyBroadcast(intent)

表现类似BehaviorSubject，会一直保留最近一次广播事件，在有新的Receiver注册之后，发送最新的广播给Receiver