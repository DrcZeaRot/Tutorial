package com.xcstasy.tutorial.algorithm.sort

import com.xcstasy.tutorial.util.logW
import kotlin.system.measureNanoTime

fun main(args: Array<String>) {
    val sort: IntSort = QuickSort()
    val array = intArrayOf(80, 60, 89, 84, 65, 73, 20, 30, 43)
    val time = measureNanoTime { sort.execute(array) }
    "Result:[$time] ${array.joinToString()}".logW()
}