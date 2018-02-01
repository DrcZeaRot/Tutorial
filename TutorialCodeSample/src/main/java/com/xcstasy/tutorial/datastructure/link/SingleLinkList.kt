package com.xcstasy.tutorial.datastructure.link

import com.xcstasy.tutorial.datastructure.Node

class SingleLinkList<E> : LinkList<E> {

    private var mCount = 0
    private var mHead: Node<E> = Node()

    override fun size(): Int = mCount

    override fun isEmpty(): Boolean = mCount == 0

    override fun get(index: Int): E? {
        return getNode(index)?.data
    }

    private fun getNode(index: Int): Node<E>? {
        if (index < 0 || index >= mCount) throw IndexOutOfBoundsException()
        var target: Node<E>? = mHead.next
        for (i in 0 until index) {
            target = target?.next
        }
        return target
    }

    override fun getFirst(): E? {
        return getNode(0)?.data
    }

    override fun getLast(): E? {
        return getNode(mCount - 1)?.data
    }

    override fun add(index: Int, data: E) {
        when (index) {
            0 -> addFirst(data)
            mCount -> addLast(data)
            else -> {
                val currentNode = getNode(index)!!
                val newNode = Node(data, currentNode.next)
                currentNode.next = newNode
                mCount++
            }
        }

    }

    override fun addFirst(data: E) {
        val newNode = Node(data, next = mHead.next)
        mHead.next = newNode
        mCount++
    }

    override fun addLast(data: E) {
        if (mCount == 0) {
            add(0, data)
        } else {
            val lastNode = getNode(mCount - 1)
            val newNode = Node(data)
            lastNode?.next = newNode
            mCount++
        }
    }

    override fun remove(index: Int) {
        if (index == 0) {
            val currentNode = getNode(0)!!
            mHead.next = currentNode.next
            clearRef(currentNode)
        } else {
            val lastNode = getNode(index - 1)!!
            val currentNode = getNode(index)!!
            lastNode.next = currentNode.next
            clearRef(currentNode)
        }
        mCount--
    }

    private fun clearRef(node: Node<E>) {
        node.data = null
        node.next = null
    }
}