package com.xcstasy.tutorial.datastructure.heap

/**
 * 注：目前无法进行理想的运行，只是模拟了《算法导论》的伪代码
 */
class MaxHeap<E : Comparable<E>> : Heap<E> {

    private var mHeap: ArrayList<E> = arrayListOf()

    private var heapSize: Int = 0

    constructor()
    constructor(source: ArrayList<E>) {
        newHeap(source)
    }

    /**
     * 堆排序
     */
    fun heapSort(source: ArrayList<E>) {
        newHeap(source)
        for (i in source.size downTo 1) {
            swap(0, 1, source)
            heapSize -= 1
            maxHeapify(i, source)
        }
    }

    private fun newHeap(source: ArrayList<E>) {
        heapSize = source.size
        mHeap = source
        for (i in source.size / 2 downTo 0) {
            maxHeapify(i, source)
        }
    }

    private fun maxHeapify(index: Int, source: ArrayList<E>) {
        val left = left(index)
        val right = right(index)
        var largest: Int

        largest = if (left <= heapSize && source[left] > source[index]) left else index
        if (right <= heapSize && source[right] > source[largest]) largest = right

        if (largest != index) {
            swap(index, largest, source)
            maxHeapify(largest, source)
        }
    }

    private fun left(index: Int): Int = index shl 1

    private fun right(index: Int): Int = (index shl 1) + 1

    private fun parent(index: Int): Int = index shr 1

    private fun swap(a: Int, b: Int, source: ArrayList<E>) {
        val temp = source[a]
        source[a] = source[b]
        source[b] = temp
    }

    override fun peek(): E? {
        return if (heapSize == 0) null else mHeap[0]
    }

    override fun poll(): E? {
        if (heapSize == 0) throw IllegalStateException()
        val max = mHeap[0]
        mHeap[0] = mHeap[heapSize]
        heapSize -= 1
        maxHeapify(0, mHeap)
        return max
    }

    override fun increase(index: Int, data: E) {
        if (heapSize <= index) throw IndexOutOfBoundsException()
        if (data < mHeap[index]) throw IllegalArgumentException("new key is smaller than current key")
        mHeap[index] = data
        var i = index
        while (i > 0 && mHeap[parent(i)] < mHeap[i]) {
            swap(i, parent(i), mHeap)
            i = parent(i)
        }
    }

    override fun insert(data: E) {
        heapSize += 1
//        mHeap[heapSize] = null 新元素设置为最小值
        increase(heapSize, data)
    }

    override fun toString(): String {
        return mHeap.joinToString { it.toString() }
    }

}