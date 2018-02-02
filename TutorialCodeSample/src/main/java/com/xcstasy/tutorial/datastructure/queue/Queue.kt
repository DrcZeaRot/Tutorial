package com.xcstasy.tutorial.datastructure.queue


interface Queue<E> {

    fun size(): Int

    fun isEmpty(): Boolean

    fun add(e: E): Boolean
    fun offer(e: E): Boolean
    /**
     * Retrieves and removes the head of this queue
     * if queue is empty, throws [NoSuchElementException]
     */
    fun remove(): E

    /**
     * Retrieves and removes the head of this queue
     * if queue is empty, return null
     */
    fun poll(): E?

    /**
     * Retrieves but does not remove the head of this queue
     * if queue is empty, throws [NoSuchElementException]
     */
    fun element(): E

    /**
     * Retrieves but does not remove the head of this queue
     * if queue is empty, return null
     */
    fun peek(): E?

    fun clear()
}