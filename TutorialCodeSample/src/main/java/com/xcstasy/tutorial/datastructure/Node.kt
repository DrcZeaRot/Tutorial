package com.xcstasy.tutorial.datastructure

class Node<E>(
        var data: E? = null,
        var pre: Node<E>? = null,
        var next: Node<E>? = null
)

class BinaryTreeNode<E : Comparable<E>>(
        var data: E,
        var leftChild: BinaryTreeNode<E>? = null,
        var rightChild: BinaryTreeNode<E>? = null
)