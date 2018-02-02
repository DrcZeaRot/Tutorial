package com.xcstasy.tutorial.datastructure.queue

class SequenceQueue<E> : Queue<E> {

    /**队首*/
    private var first: Int = 0
    /**队尾*/
    private var last: Int = 0

    private var elements: Array<E?>

    private var elementCount: Int = 0

    private var capacity: Int = 8

    private var capacityIncrement: Int = 8

    constructor() : this(8, 8)
    constructor(capacity: Int) : this(capacity, 8)
    constructor(capacity: Int, capacityIncrement: Int) {
        this.capacity = capacity
        this.capacityIncrement = capacityIncrement
        elements = arrayOfNulls<Any>(capacity) as Array<E?>
    }


    override fun size(): Int = elementCount

    override fun isEmpty(): Boolean = first == last

    override fun add(e: E): Boolean {
        if (isFull()) {
            ensureCapacity(capacity + capacityIncrement)
        }
        //元素放到队尾
        elements[last] = e
        //移动队尾到下一个位置
        last = (last + 1).rem(elements.size)
        elementCount++
        return true
    }

    override fun offer(e: E): Boolean {
        if (isFull()) {
            throw IllegalArgumentException("The capacity of queue has reached its maximum")
        }
        elements[last] = e
        last = (last + 1).rem(elements.size)
        elementCount++
        return true
    }

    private fun ensureCapacity(capacity: Int) {
        if (capacity <= elementCount) return
        val oldElements = elements
        val newElements = arrayOfNulls<Any>(capacity)

        if (last < first) {
            //从队首开始拷贝，一直到数组末端
            System.arraycopy(oldElements, first,
                    newElements, 0, elementCount - first + 1)
            //如果队尾在队首前，则继续拷贝从数组前端到队尾的部分
            System.arraycopy(oldElements, 0,
                    newElements, elementCount - first + 1, last)
        } else {
            System.arraycopy(oldElements, first,
                    newElements, 0, elementCount - first)
        }
        //定位队首、队尾
        first = 0
        last = elementCount
        elements = newElements as Array<E?>
    }

    private fun isFull(): Boolean {
        return (last + 1).rem(elements.size) == first
    }

    override fun remove(): E {
        if (isEmpty()) {
            throw NoSuchElementException("The queue is empty")
        }
        return poll()!!
    }

    override fun poll(): E? {
        if (isEmpty()) return null
        val front = elements[first]
        first = (first + 1).rem(elements.size)
        elementCount--
        return front
    }

    override fun element(): E {
        if (isEmpty()) {
            throw NoSuchElementException("The queue is empty")
        }
        return peek()!!
    }

    override fun peek(): E? {
        if (isEmpty()) return null
        return elements[first]
    }

    override fun clear() {
        var i = first
        while (i != last) {
            elements[i] = null
            i = (i + 1).rem(elements.size)
        }
        first = 0
        last = 0
        elementCount = 0
    }

}