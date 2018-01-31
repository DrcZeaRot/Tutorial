package com.xcstasy.tutorial.datastructure.stack

import com.xcstasy.tutorial.util.logW

fun main(args: Array<String>) {
    val stack:Stack<String> = SequenceStack()

    stack.push("99")
    stack.push("00")
    stack.push("44")
    stack.push("33")
    stack.push("44")
    stack.push("33")
    stack.push("44")
    stack.push("33")
    stack.push("33")
    stack.push("77")
    stack.push("55")
    stack.push("22")
    val size = stack.size()
    for(i in 0 until size){
        stack.pop()?.let {
            "Index: $i Value:$it".logW()
        }
    }
}