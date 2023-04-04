package isel.leic.pc.lec_04_04

import mu.KotlinLogging
import java.lang.Exception
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

private val logger = KotlinLogging.logger {}

class SimpleThreadPool(val maxThreadPoolSize : Int) {

    fun execute(runnable: Runnable) {
         TODO()
    }

}