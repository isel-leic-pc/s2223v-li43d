package isel.leic.pc.lec_04_13.lock_free

import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

private var number = 0
private var ready = AtomicBoolean(false)

fun main() {
    logger.info("Start!")

    val t = thread {
        while(!ready.get()) {} // println("false");
        logger.info("number: {}", number)
    }

    Thread.sleep(100)

    number = 42
    ready.set(true)

    t.join()
    logger.info("Done!")
}