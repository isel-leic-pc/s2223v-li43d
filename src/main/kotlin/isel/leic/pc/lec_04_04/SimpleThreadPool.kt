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

    private fun safeRun(work: Runnable) {
        try {
            work.run()
        }
        catch( t : Throwable) {
            // for now we just swallow the thrpwable
            // but we could save it in a log
            // here we can do it because this is extenal code
            // that should heve no impact on the pool threads
        }
    }

    private fun workerFun(work: Runnable) {

        var currWork = work
        do {
            safeRun(currWork)
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