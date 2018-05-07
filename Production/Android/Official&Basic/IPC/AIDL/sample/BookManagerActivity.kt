package com.xcstasy.r.larva.sample.aidltester

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.xcstasy.r.larva.sample.aidl.IBookManager

/**
 * @author Drc_ZeaRot
 * @since 2018/5/4
 * @lastModified by Drc_ZeaRot on 2018/5/4
 */
class BookManagerActivity : AppCompatActivity() {

    private var mBookManager: IBookManager? = null

    private val mConnection: ServiceConnection = object : ServiceConnection {
        /**
         * 可以在服务连接断开时，重新连接，保证Binder连接不会断开。
         */
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mBookManager = IBookManager.Stub.asInterface(service)
            mBookManager?.asBinder()?.linkToDeath(mDeathRecipient, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind2BookService()
    }

    private fun bind2BookService() {
        bindService(Intent(this, BookService::class.java), mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        unbindService(mConnection)
        super.onDestroy()
    }

    private val mDeathRecipient = Recipient()

    /**
     * Binder运行在服务端进程，如果服务进程由于某种原因异常终止，<br/>
     * 我们到服务端的Binder连接断裂(称为Binder死亡)，导致远程调用失败。<br/>
     * 通过link/unlinkToDeath，设置、取消死亡代理。<br/>
     * Binder死亡，binderDied方法就会回调。可以重新连接Binder。<br/>
     * Binder::isBinderAlive也可以用于判断Binder是否死亡。
     */
    inner class Recipient : IBinder.DeathRecipient {
        override fun binderDied() {
            mBookManager?.asBinder()?.unlinkToDeath(mDeathRecipient, 0)
            mBookManager = null
            bind2BookService()
        }
    }

}