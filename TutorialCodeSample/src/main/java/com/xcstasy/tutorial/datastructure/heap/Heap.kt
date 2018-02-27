package com.xcstasy.tutorial.datastructure.heap

interface Heap<E : Comparable<E>> {
    /**
     * 查看最大值
     */
    fun peek(): E?

    /**
     * 查看并移除最大值
     */
    fun poll(): E?

    /**
     * 增大index处的元素
     */
    fun increase(index: Int, data: E)

    /**
     * 插入一个新的元素
     */
    fun insert(data: E)
}