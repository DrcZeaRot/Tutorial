// IBookManager.aidl
package com.xcstasy.r.larva.sample.aidl;

/**
* 即使声明在同一个包内，也要显示地引用正确的类。
*/
import com.xcstasy.r.larva.sample.aidl.Book;
import java.util.List;

interface IBookManager {
    List<Book> getBookList();

    /**
    * 参数需要正确的 "in" 、"out"、"inout" 定向tag
    */
    void addBook(in Book book);
}
