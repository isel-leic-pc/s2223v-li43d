package isel.leic.pc.lec_04_04

import mu.KotlinLogging

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

private val logger = KotlinLogging.logger {}

class SimpleThreadPool(val maxThreadPoolSize : Int) {
    private val monitor = ReentrantLock()

    private val pendingWork = LinkedList<Runnable>()

    private var workers = 0

    private fun terminate() {
        workers--
    }

    private fun workerFun(work: Runnable) {

        var currWork = work
        do {
            currWork.run()
            monitor.withLock {
                if (pendingWork.size > 0) {
                    currWork = pendingWork.poll()
                } else {
                    terminate()
                    return
                }
            }

        }
        while(true)

    }

    fun execute(runnable: Runnable) {
        monitor.withLock {
            if (workers < maxThreadPoolSize)  {
                workers++
                thread {
                   workerFun(runnable)
                }
            }
            else {
                pendingWork.add(runnable)
            }
        }
    }

}