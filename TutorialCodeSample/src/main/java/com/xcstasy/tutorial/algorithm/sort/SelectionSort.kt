package com.xcstasy.tutorial.algorithm.sort

import com.xcstasy.tutorial.util.logW
import kotlin.system.measureNanoTime

class SelectionSort :IntSort{
    override fun execute(source: IntArray) {
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