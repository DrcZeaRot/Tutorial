package com.xcstasy.tutorial.datastructure.link

import com.xcstasy.tutorial.util.logW

fun main(args: Array<String>) {
    val list: LinkList<String> = DoubleLinkList()
    list.addLast("10")
    list.addLast("13")
    list.addFirst("13")
    list.addFirst("13")
    list.addFirst("11")
    list.addFirst("15")
    list.addLast("15")
    list.addLast("15")
    list.addLast("15")

    list.remove(4)

    for (i in 0 until list.size()) {
        "Result: ${list.get(i)}".logW()
    }
}