package isel.leic.pc.lec_05_16.coroutines

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val logger = KotlinLogging.logger {}

class BuildersTests {


    @Test
    fun `global scope launch_test`() {
        val latch = CountDownLatch(1)

        GlobalScope.launch {
            logger.info("before delay")
            delay(3000)
            logger.info("after delay")
            latch.countDown()
        }
        latch.await()
        logger.info("done!")
    }

    @Test
    fun `runBlocking_launch_test`() {

        logger.info("before runBloking!")
        runBlocking {
            GlobalScope.launch {
                logger.info("before delay")
                delay(3000)
                logger.info("after delay")
            }

            GlobalScope.launch {
                logger.info("before delay")
                delay(3000)
                logger.info("after delay")
            }

        }
        logger.info("after runBloking!")
        sleep(4000)
        logger.info("done!")
    }

    private suspend fun noReturn() : Unit {
        logger.info("before suspention point")
        suspendCoroutine<Unit> {

        }
        logger.info("after suspention point")
    }

    private suspend fun immediateReturn() : Unit {
        logger.info("before suspention point")
        suspendCoroutine<Unit> { continuation ->
            continuation.resume(Unit)
        }
        logger.info("after suspention point")
    }

    private val schedulerThread =
        Executors.newSingleThreadScheduledExecutor()

    private suspend fun myDelay(millis: Long) {
        suspendCoroutine<Unit> { continuation ->
            logger.info("before suspention point")
            schedulerThread.schedule( {
                sleep(millis)
                logger.info("resuming point")
                continuation.resume((Unit))
            }, millis, TimeUnit.MILLISECONDS)
        }
        logger.info("after suspention point")
    }

    @Test
    fun `runBlocking_mutiple_launch_using_runBlocking_scope_test`() {

        logger.info("before runBloking!")
        runBlocking {
            this.launch {
                logger.info("before delay")
                delay(3000)
                logger.info("after delay")
            }

            launch {
                logger.info("before delay")
                delay(3000)
                logger.info("after delay")
            }

        }
        logger.info("after runBloking!")

        logger.info("done!")
    }


    @Test
    fun `check_no_return_suspend_func`() {
        runBlocking {
            noReturn()
        }
    }

    @Test
    fun `check_immediate_return_suspend_func`() {
        runBlocking {
            immediateReturn()
        }
    }

    @Test
    fun `check_myDelay_suspend_func`() {
        runBlocking {

            myDelay(5000)
        }
    }

}