package com.xcstasy.tutorial.util

import java.time.Instant

fun <T : Any> T.logW() {
    println("[${Instant.now()}] [${Thread.currentThread().name}] ${this}")
}