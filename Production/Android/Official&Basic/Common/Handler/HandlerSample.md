##### 任意线程创建Handler

    lateinit var handler: Handler
    object : Thread() {
        override fun run() {
            Looper.prepare()
            handler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    Log.w("TAG","handleMessage in Non-MainThread")
                }
            }
            Looper.loop()
        }
    }.start()
    handler.sendEmptyMessage(0)


##### HandlerThread使用
    lateinit var handlerThread: HandlerThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlerThread = HandlerThread("MyHandlerThread")
        handlerThread.start()
        val handler = object :Handler(handlerThread.looper){
            override fun handleMessage(msg: Message?) {
                Log.i("TAG","handleMessage in HandlerThread")
            }
        }

        object :Thread(){
            override fun run() {
                handler.sendEmptyMessage(0)
            }
        }.start()
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            handlerThread.quitSafely()
        } else {
            handlerThread.quit()
        }
        super.onDestroy()
    }