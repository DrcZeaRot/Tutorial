package com.xcstasy.tutorial.datastructure.link

interface LinkList<E> {
    fun size(): Int
    fun isEmpty(): Boolean
    fun get(index: Int): E?
    fun getFirst(): E?
    fun getLast(): E?
    fun add(index: Int, data: E)
    fun addFirst(data:E)
    fun addLast(data:E)
    fun remove(index: Int)
}