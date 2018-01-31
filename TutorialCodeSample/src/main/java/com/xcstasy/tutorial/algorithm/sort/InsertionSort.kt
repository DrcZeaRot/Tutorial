package com.xcstasy.tutorial.algorithm.sort

import com.xcstasy.tutorial.util.logW
import kotlin.system.measureNanoTime

class InsertionSort {
    fun execute(source: IntArray) {
        var pivot: Int
        var inner: Int
        val size = source.size
        for (outer in 1 until size) {
            pivot = source[outer]
            inner = outer
            while (inner > 0 && source[inner - 1] > pivot){
                source[inner] = source[--inner]
            }
            source[inner] = pivot
        }
    }
}

fun main(args: Array<String>) {
    val sort = InsertionSort()
    val array = intArrayOf(80, 60, 89, 84, 65, 73, 20, 30, 43)
    val time = measureNanoTime { sort.execute(array) }
    "Result:[$time] ${array.joinToString()}".logW()
}