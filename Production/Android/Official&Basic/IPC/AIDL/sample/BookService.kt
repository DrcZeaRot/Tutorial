package com.xcstasy.r.larva.sample.aidltester

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.xcstasy.r.larva.sample.aidl.Book
import com.xcstasy.r.larva.sample.aidl.IBookManager
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Drc_ZeaRot
 * @since 2018/5/4
 * @lastModified by Drc_ZeaRot on 2018/5/4
 */
class BookService : Service() {

    private val mBookList: CopyOnWriteArrayList<Book> = CopyOnWriteArrayList()

    override fun onBind(intent: Intent?): IBinder = mBinder

    override fun onCreate() {
        super.onCreate()
        mBookList.add(Book(1, "Android"))
        mBookList.add(Book(2, "iOS"))
    }

    private val mBinder: IBinder = object : IBookManager.Stub() {
        override fun getBookList(): MutableList<Book> = mBookList

        override fun addBook(book: Book) {
            mBookList.add(book)
        }
    }

}