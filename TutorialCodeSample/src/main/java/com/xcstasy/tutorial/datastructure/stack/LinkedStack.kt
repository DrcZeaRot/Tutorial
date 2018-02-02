package com.xcstasy.tutorial.datastructure.stack

import com.xcstasy.tutorial.datastructure.Node

class LinkedStack<E> : Stack<E> {

    private var top: Node<E>? = Node()

    private var elementCount: Int = 0

    override fun size(): Int = elementCount

    override fun isEmpty(): Boolean = top == null || top?.data == null

    override fun push(data: E) {
        top.also {
            when {
                it == null -> top = Node(data)
                it.data == null -> it.data = data
                else -> {
                    val pre = Node(data, pre = top)
                    top = pre
                }
            }
        }
        elementCount++
    }

    override fun pop(): E? {
        return if (isEmpty()) {
            null
        } else {
            top?.let {
                val data = it.data
                top = it.pre
                elementCount--
                data
            }
        }
    }

    override fun peek(): E? = top?.data

    override fun search(data: E): Int {
        var target: Node<E>? = top
        for (i in 0 until elementCount) {
            if (data == target?.data) {
                return i
            } else target = target?.pre
        }
        return -1
    }

}