package com.xcstasy.tutorial.datastructure.tree

interface Tree<E : Comparable<E>> {
    fun insert(e: E): Boolean
    fun delete(e: E): Boolean
}