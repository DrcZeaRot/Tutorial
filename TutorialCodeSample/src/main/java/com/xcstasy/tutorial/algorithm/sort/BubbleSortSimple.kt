package com.xcstasy.tutorial.algorithm.sort

class BubbleSortSimple :IntSort{
    override fun execute(source: IntArray) {
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