package isel.leic.pc.lec_05_02

import isel.leic.pc.lec_05_02.completable_futures.oper1Async
import isel.leic.pc.lec_05_02.completable_futures.oper2Async
import isel.leic.pc.lec_05_02.completable_futures.oper3Async
import mu.KotlinLogging
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.CompletableFuture
import kotlin.test.assertFails

private val logger = KotlinLogging.logger {}

class CompletableFuturesTests {
    @Test
    fun `compose_with_success`() {

        val fut = oper1Async()
        .thenCompose {
           oper2Async(it)
        }
        .thenCompose {
           oper3Async(it)
        }


        logger.info("done with result = ${fut.get()}")

        assertEquals(3, fut.get())
    }

    @Test
    fun `compose_with_error`() {
        val fut = oper1Async()
            .thenCompose {
                oper2Async(-1)
            }
            .thenCompose {
                oper3Async(it)
            }
            .whenComplete { res, error ->
                if (error != null)
                    logger.info("error on completable chain!")
            }
        try {
            logger.info("done with result = ${fut.get()}")
            fail("Should have thrown exeception")
        }
        catch(e: Exception) {
            logger.info("exception throwed on get")
        }

    }

    @Test
    fun `compose_with_resolved_error`() {
        val fut = oper1Async()
            .thenCompose {
                oper2Async(-1)
            }
            .thenCompose {
                oper3Async(it)
            }
            .whenComplete { res, error ->
                if (error != null)
                    logger.info("error on completable chain!")
            }
            .exceptionally { error ->
                0
            }


        assertEquals(0, fut.get())

    }


    @Test
    fun `combinator all test`() {
        val cf1 = oper1Async()
        val cf2 = oper2Async(-1)
        val cf3 = oper3Async("teste")

        val fut = CompletableFuture.allOf(
            cf1,
            cf2,
            cf3
        )
        .whenComplete {
                v, e ->
            if (e != null) {
                logger.info("final error: $e")

            }
        }

        try {
            logger.info("done with result = ${fut.get()}")
            fail("Should have thrown exeception")
        }
        catch(e: Exception) {
            logger.info("exception throwed on get")
        }
    }



}