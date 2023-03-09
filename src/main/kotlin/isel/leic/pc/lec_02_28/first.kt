package isel.leic.pc

import kotlin.concurrent.thread

private fun threadFunc() {
    Thread.sleep(2000)
    println("hello from thread ${Thread.currentThread().name}")
}

private fun main() {
    // a daemon thread doesn't force process lifetime
    val t1 = thread(isDaemon = true,  block = ::threadFunc)

    val t2 = thread(isDaemon = true) {
        Thread.sleep(2000)
        println("hello from thread ${Thread.currentThread().name}")
    }

    t1.join()
    t2.join()

    println("main thread name: ${Thread.currentThread().name}")
}