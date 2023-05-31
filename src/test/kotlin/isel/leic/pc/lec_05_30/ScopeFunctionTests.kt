package isel.leic.pc.lec_05_30

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.DefaultAsserter.fail
import kotlin.test.fail

private val logger = KotlinLogging.logger {}

class ScopeFunctionTests {

    private suspend fun inc(n : Int) : Int {
        delay(1000)
        return n + 1
    }

    private suspend fun parInc(pair : Pair<Int,Int>, withError : Boolean = false) : Pair<Int,Int> {
        return coroutineScope {
            val df1 = async {
                delay(1000)
                if (withError)
                    throw Error("Simulated error")
                inc(pair.first)
            }

            val df2 = async {
                try {
                    delay(2000)
                    inc(pair.second)
                }
                catch(e: CancellationException) {
                    logger.info("error $e occurred on child coroutine")
                }
                inc(pair.second)
            }

            Pair(df1.await(), df2.await())
       }
    }

    data class MutPair<T,U> (var first: T, var second: U)

    private suspend fun parIncWithLaunch(pair : MutPair<Int,Int>, withError : Boolean = false)
                            = supervisorScope {
        launch {
            delay(1000)
            if (withError)
                throw Error("Simulated error")
            pair.first = inc(pair.first)
        }

        launch {
            try {
                delay(2000)
                pair.second =   inc(pair.second)
            }
            catch(e: CancellationException) {
                logger.info("error $e occurred on child coroutine")
            }
            inc(pair.second)
        }

    }

    @Test
    fun `use coroutineScope to parallelize coroutines`() {
        runBlocking {
            try {
                val res = parInc(Pair(3, 4), withError = true)

                assertEquals(Pair(4, 5), res)
            }
            catch(e: Throwable) {
                logger.info("test end with error $e")
            }
        }
    }


    @Test
    fun `use coroutineScope to parallelize coroutines with error`() {
        runBlocking {
            val res = parInc(Pair(3,4))

            assertEquals(Pair(4, 5), res)
        }
    }

    @Test
    fun `use coroutineScope to parallelize coroutines with launch`() {
        runBlocking {
            try {
                val mp = MutPair(3, 4)
                parIncWithLaunch(mp)

                assertEquals(MutPair(4, 5), mp)
            }
            catch(e: Throwable) {
                logger.info("test end with error $e")
            }
        }
    }

    @Test
    fun `use coroutineScope to parallelize coroutines with launch with error`() {
            val errorHandler = CoroutineExceptionHandler { ctx, err ->
                logger.info("error cauch by handler: $err")
            }

            runBlocking(errorHandler) {
                try {
                    val mp = MutPair(3, 4)
                    parIncWithLaunch(mp, withError = true)

                    assertEquals(MutPair(3, 5), mp)
                }
                catch(e: Throwable) {
                    logger.info("test end with error $e")
                    fail()
                }
            }

    }

    @Test
    fun `exec suspend with timeout`() {
        runBlocking {
            try {
                val res = withTimeout(500) {
                    inc(2)
                }

                assertEquals(3, res)
            }
            catch(e: Throwable) {
                logger.info("timeout occurred: error $e")
            }
        }
    }

    @Test
    fun `exec coroutine opn a different scheduler`() {
        runBlocking {
            logger.info("exec runBlocking")
            val res = withContext(Dispatchers.IO) {
                logger.info("exec WithContext coroutine")
                inc(2)
            }

            assertEquals(3, res)
        }
    }
}