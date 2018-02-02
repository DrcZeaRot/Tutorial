package com.xcstasy.tutorial.datastructure.queue

import com.xcstasy.tutorial.util.logW

fun main(args: Array<String>) {
    val queue: Queue<String> = SingleLinkedQueue()
    queue.add("10")
    queue.add("20")
    queue.add("50")
    queue.add("70")
    queue.add("70")
    queue.remove()
    queue.remove()
    queue.remove()
    queue.remove()
    queue.add("90")
    queue.add("30")
    queue.add("40")
    queue.add("40")
    queue.add("40")
    queue.add("000")
    queue.add("420")
    queue.add("430")

    for (i in 0 until queue.size()) {
        "Index: ${queue.poll()}".logW()
    }
}