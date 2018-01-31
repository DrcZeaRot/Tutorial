package com.xcstasy.tutorial.datastructure.stack

import com.xcstasy.tutorial.datastructure.Node

class LinkedStack<E> : Stack<E> {

    private var top: Node<E>? = null

    private var size: Int = 0

    init {
        top = Node()
    }

    override fun size(): Int = size

    override fun isEmpty(): Boolean = top == null || top?.data == null

    override fun push(data: E) {

        size++
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pop(): E? {
        return if (isEmpty()) {
            null
        } else {
            top?.let {
                val data = it.data
                top = it.next
                size--
                data
            }
        }
    }

    override fun peek(): E? = top?.data

    override fun search(data: E): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}