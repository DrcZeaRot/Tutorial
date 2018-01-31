package com.xcstasy.tutorial.datastructure.stack

interface Stack<E> {

    /**栈的大小*/
    fun size():Int

    /**为空判断*/
    fun isEmpty(): Boolean

    /**入栈*/
    fun push(data: E)

    /**出栈*/
    fun pop(): E?

    /**仅仅是得到栈顶元素，并不做出栈操作*/
    fun peek(): E?

    /**元素在栈中的位置， 下标从0开始*/
    fun search(data: E): Int
}