package com.xcstasy.tutorial.algorithm.sort

import com.xcstasy.tutorial.util.logW
import kotlin.system.measureNanoTime

class DoubleBubbleSort {
    fun execute(source: IntArray) {
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

fun main(args: Array<String>) {
    val sort = DoubleBubbleSort()
    val array = intArrayOf(80, 60, 89, 84, 65, 73, 20, 30, 43)
    val time = measureNanoTime { sort.execute(array) }
    "Result:[$time] ${array.joinToString()}".logW()
}