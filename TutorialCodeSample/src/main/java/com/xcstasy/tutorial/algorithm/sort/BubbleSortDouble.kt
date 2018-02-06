package com.xcstasy.tutorial.algorithm.sort

class BubbleSortDouble : IntSort {
    override fun execute(source: IntArray) {
        var temp: Int
        var index = 0
        var left = 0
        var right = source.size - 1

        while (index <= right) {
            for (j in index until right) {
                if (source[j] > source[j + 1]) {
                    temp = source[j + 1]
                    source[j + 1] = source[j]
                    source[j] = temp
                }
            }
            right--
            for (j in right - 1 downTo left + 1) {
                if (source[j] < source[j - 1]) {
                    temp = source[j]
                    source[j] = source[j - 1]
                    source[j - 1] = temp
                }
            }
            left++
            index++
        }
    }
}