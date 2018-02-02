package com.xcstasy.tutorial.datastructure.stack

class SequenceStack<E> : Stack<E> {

    /**元素数组*/
    private var elements: Array<E>
    /**容量*/
    private var capacity: Int = 8
    /**扩容大小*/
    private var capacityIncrement: Int = 8
    /**栈内元素数*/
    private var elementCount: Int = 0

    constructor() : this(8, 8)
    constructor(capacity: Int) : this(capacity, 8)
    constructor(capacity: Int, capacityIncrement: Int) {
        elements = arrayOfNulls<Any>(capacity) as Array<E>
        this.capacity = capacity
        this.capacityIncrement = capacityIncrement
    }

    override fun size(): Int = elementCount

    override fun isEmpty(): Boolean = elementCount == 0

    override fun push(data: E) {
        if (capacity == elementCount) {
            ensureSize(capacity + capacityIncrement)
        }
        elements[elementCount++] = data
    }

    private fun ensureSize(capacity: Int) {
        if (capacity <= elementCount) return
        val newElements = arrayOfNulls<Any>(capacity)
        val oleElements = elements
        System.arraycopy(oleElements, 0, newElements, 0, this.capacity)
        elements = newElements as Array<E>
    }

    override fun pop(): E? {
        return if (isEmpty()) {
            null
        } else {
            val element = elements[elementCount - 1]
            elementCount--
            element
        }

    }

    override fun peek(): E? {
        return if (isEmpty()) null else elements[elementCount - 1]
    }

    override fun search(data: E): Int {
        for (i in elements.size - 1 downTo 0) {
            if (elements[i] == data) return size() - i - 1
        }
        return -1
    }

}