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
            ensureSize()
        }
        elements[elementCount++] = data
    }

    private fun ensureSize() {
        val newElements = arrayOfNulls<Any>(capacity + capacityIncrement)
        System.arraycopy(elements, 0, newElements, 0, capacity)
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
        for (i in 0 until elements.size) {
            if (elements[i] == data) return i
        }
        return -1
    }

}