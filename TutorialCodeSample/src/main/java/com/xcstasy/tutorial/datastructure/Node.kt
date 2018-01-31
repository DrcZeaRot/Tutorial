package com.xcstasy.tutorial.datastructure

class Node<E>(
        var data: E? = null,
        var pre: Node<E>? = null,
        var next: Node<E>? = null
)