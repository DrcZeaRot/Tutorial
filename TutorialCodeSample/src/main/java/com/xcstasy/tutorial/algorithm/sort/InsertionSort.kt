package com.xcstasy.tutorial.algorithm.sort

import com.xcstasy.tutorial.util.logW
import kotlin.system.measureNanoTime

class InsertionSort :IntSort{
    override fun execute(source: IntArray) {
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