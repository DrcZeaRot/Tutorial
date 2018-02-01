package com.xcstasy.tutorial.datastructure.link

import com.xcstasy.tutorial.datastructure.Node

/**
 * 双向循环链表
 */
class CycleLinkList<E> : LinkList<E> {

    private var mCount = 0
    private var mHead: Node<E> = Node()

    init {
        mHead.next = mHead
        mHead.pre = mHead
    }


    override fun size(): Int = mCount

    override fun isEmpty(): Boolean = mCount == 0

    override fun get(index: Int): E? {
        return getNode(index)?.data
    }

    private fun getNode(index: Int): Node<E>? {
        if (index < 0 || index >= mCount) throw IndexOutOfBoundsException()
        //正向查找
        if (index <= mCount / 2) {
            var next: Node<E>? = mHead.next
            for (i in 0 until index) {
                next = next?.next
            }
            return next
        }
        //反向查找
        var previous: Node<E>? = mHead.pre
        val reverseIndex = mCount - index - 1
        for (i in 0 until reverseIndex) {
            previous = previous?.pre
        }
        return previous
    }

    override fun add(index: Int, data: E) {
        when (index) {
            0 -> addFirst(data)
            mCount -> addLast(data)
            else -> {
                val currentNode = getNode(index)!!
                val newNode = Node(data, pre = currentNode.pre, next = currentNode)
                currentNode.pre?.next = newNode
                currentNode.next = newNode
                mCount++
            }
        }
    }

    override fun remove(index: Int) {
        val currentNode = getNode(index)
        currentNode?.apply {
            pre?.next = currentNode.next
            next?.pre = currentNode.pre
            clearRef(this)
            mCount--
        }
    }

    override fun getFirst(): E? {
        return get(0)
    }

    override fun getLast(): E? {
        return get(mCount - 1)
    }

    override fun addFirst(data: E) {
        val newNode = Node(data, pre = mHead, next = mHead.next)
        mHead.next?.pre = newNode
        mHead.next = newNode
        mCount++
    }

    override fun addLast(data: E) {
        val newNode = Node(data, pre = mHead.pre, next = mHead)
        mHead.pre?.next = newNode
        mHead.pre = newNode
        mCount++
    }

    private fun clearRef(node: Node<E>) {
        node.data = null
        node.next = null
        node.pre = null
    }
}