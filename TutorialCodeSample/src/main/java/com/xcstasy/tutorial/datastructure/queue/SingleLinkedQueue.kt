package com.xcstasy.tutorial.datastructure.queue

import com.xcstasy.tutorial.datastructure.Node

class SingleLinkedQueue<E> : Queue<E> {

    private var elementCount: Int = 0

    private var capacity: Int

    private var first: Node<E>? = null
    private var last: Node<E>? = null

    constructor() : this(128)
    constructor(capacity: Int) {
        this.capacity = capacity
    }

    override fun size(): Int = elementCount

    override fun isEmpty(): Boolean = first == null && last == null

    override fun add(e: E): Boolean {
        val node = Node(e)
        if (first == null) {//空队列插入
            first = node
        } else {//非空队列插入
            last?.next = node
        }
        last = node
        elementCount++
        return true
    }

    override fun offer(e: E): Boolean {
        if (elementCount >= capacity)
            throw IllegalArgumentException("The capacity of queue has reached its maximum")
        val node = Node(e)
        if (first == null) {//空队列插入
            first = node
        } else {//非空队列插入
            last?.next = node
        }
        last = node
        elementCount++
        return true
    }

    override fun remove(): E {
        if (isEmpty()) throw NoSuchElementException("The queue is empty")
        val data = first?.data
        first = first?.next
        if (first == null) {
            last = null
        }
        elementCount--
        return data!!
    }

    override fun poll(): E? {
        if (isEmpty()) return null
        val data = first?.data
        first = first?.next
        if (first == null) {
            last = null
        }
        elementCount--
        return data
    }

    override fun element(): E {
        return if (isEmpty()) throw NoSuchElementException("The queue is empty") else first?.data!!
    }

    override fun peek(): E? {
        return if (isEmpty()) null else first?.data
    }

    override fun clear() {
        first = null
        last = null
        elementCount = 0
    }
}