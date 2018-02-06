package com.xcstasy.tutorial.algorithm.sort

interface IntSort {
    fun execute(source: IntArray)
}

interface Sort<E> {
    fun execute(source: Array<E>)
}