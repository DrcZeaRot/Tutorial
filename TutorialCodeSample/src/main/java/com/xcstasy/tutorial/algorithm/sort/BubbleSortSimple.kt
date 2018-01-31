package com.xcstasy.tutorial.algorithm.sort

import com.xcstasy.tutorial.util.logW
import kotlin.system.measureNanoTime

class SimpleBubbleSort {
    fun execute(source: IntArray) {
        var temp: Int
        val size = source.size
        for (outer in 0 until size) {
            for (inner in 0 until size - 1 - outer) {
                if (source[inner] > source[inner + 1]) {
                    temp = source[inner + 1]
                    source[inner + 1] = source[inner]
                    source[inner] = temp
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    val sort = SimpleBubbleSort()
    val array = intArrayOf(80, 60, 89, 84, 65, 73, 20, 30, 43)
    val time = measureNanoTime { sort.execute(array) }
    "Result:[$time] ${array.joinToString()}".logW()
}