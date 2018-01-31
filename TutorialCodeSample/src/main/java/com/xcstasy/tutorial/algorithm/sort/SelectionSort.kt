package com.xcstasy.tutorial.algorithm.sort

import com.xcstasy.tutorial.util.logW
import kotlin.system.measureNanoTime

class SelectionSort {
    fun execute(source: IntArray) {
        var min: Int
        var temp: Int
        val size = source.size
        for (outer in 0 until size) {
            min = outer
            for (inner in outer until size) {
                if (source[inner] < source[min]) {
                    min = inner
                }
            }
            temp = source[outer]
            source[outer] = source[min]
            source[min] = temp

        }
    }
}

fun main(args: Array<String>) {
    val sort = SelectionSort()
    val array = intArrayOf(80, 60, 89, 84, 65, 73, 20, 30, 43)
    val time = measureNanoTime { sort.execute(array) }
    "Result:[$time] ${array.joinToString()}".logW()
}