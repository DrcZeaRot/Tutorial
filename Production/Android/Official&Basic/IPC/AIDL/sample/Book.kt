package com.xcstasy.r.larva.sample.aidl

import android.os.Parcel
import android.os.Parcelable

/**
 * 按照正常的Parcelable模板生成，再手动添加一个方法。
 */
class Book(val bookId: Int, val bookName: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()
    )

    /**
     * 需要手动添加一个readFromParcel的方法
     */
    fun readFromParcel(parcel: Parcel) {
        bookId = parcel.readInt()
        bookName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(bookId)
        parcel.writeString(bookName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Book> {
        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }
}