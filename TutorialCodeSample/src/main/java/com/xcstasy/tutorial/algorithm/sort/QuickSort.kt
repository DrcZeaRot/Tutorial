package com.xcstasy.tutorial.algorithm.sort

class QuickSort : IntSort {
    override fun execute(source: IntArray) {
        val size = source.size
        if (size <= 3) {
            manualSort(source)
        } else {
            recQuickSort(0, source.size - 1, source)
        }
    }

    /**
     * 3个元素一下，手动排序
     */
    private fun manualSort(source: IntArray) {
        val size = source.size
        val left = 0
        val right = source.size - 1
        if (size <= 1) return
        if (size == 2) {
            if (source[left] > source[right]) swap(left, right, source)
        } else {
            if (source[left] > source[right - 1]) swap(left, right - 1, source)//left center
            if (source[left] > source[right]) swap(left, right, source)//left right
            if (source[right - 1] > source[right]) swap(right - 1, right, source)//center right
        }
    }

    private fun recQuickSort(left: Int, right: Int, source: IntArray) {
        if (left >= right) return
        val partition = partition(left, right, source)
        recQuickSort(left, partition - 1, source)
        recQuickSort(partition + 1, right, source)
    }

    /**
     * 划分
     */
    private fun partition(left: Int, right: Int, source: IntArray): Int {
        val pivot = medianOf3(left, right, source)//取最后的元素作为参考
        var leftPtr = left
        var rightPtr = right
        while (leftPtr < rightPtr) {
            while (leftPtr <= right && source[leftPtr] < pivot) {
                leftPtr++
            }
            while (rightPtr >= 0 && source[rightPtr] > pivot) {
                rightPtr--
            }
            swap(leftPtr, rightPtr, source)
        }
        source[leftPtr] = pivot
        return leftPtr
    }

    /**
     * 从head、center、tail中获取最合适的作为pivot
     */
    private fun medianOf3(left: Int, right: Int, source: IntArray): Int {
        val center = (left + right) / 2
        if (source[left] > source[center]) swap(left, center, source)
        if (source[left] > source[right]) swap(left, right, source)
        if (source[center] > source[right]) swap(center, right, source)
        swap(center, right - 1, source)
        return source[right - 1]
    }

    private fun swap(a: Int, b: Int, source: IntArray) {
        val temp = source[a]
        source[a] = source[b]
        source[b] = temp
    }
}