package isel.leic.pc.lec_05_23

import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

class SuspendTests {

    private fun getValue() : CompletableFuture<Int> {
        return CompletableFuture.supplyAsync {
            try {
                sleep(50000)
                5
            }
            catch(e: Throwable) {
                println("future cancelled!")
                throw e
            }
        }
    }


    suspend private fun getValueSuspend() : Int {
        // use of await CompletableFuture extension function
        // done in this lecture
        return getValue().await()
    }

    @Test
    fun `test suspend wrapper for function returning completable future`() {
        runBlocking {
            val child = launch {
                try {
                    val v = getValueSuspend()
                    println("v=$v")
                }
                catch(e: Throwable ) {
                    println("error on child: $e")
                }
            }
            delay(1000)
            child.cancel()
        }
    }








    private suspend fun getValue(value: Int) : Int {
        if (value == 10) throw Error("value production failed")
        delay(500)
        return value
    }

    private suspend fun multipleSuspendCallsWithAutoScope()
                    = coroutineScope {

        val v1 = async {
            delay(1000)
            getValue(10)
        }

        val v2 = async {

            val v = getValue(5)
            println("v2 produce v!")
            v
        }

        v1.await() + v2.await()

    }


    private suspend fun multipleSuspendCalls() : Int {
        val scope = CoroutineScope(Job() + Dispatchers.IO)

        val v1 = scope.async {
            delay(1000)
            getValue(10)
        }

        val v2 = scope.async {

            val v = getValue(5)
            println("v2 produce v!")
            v
        }

        return v1.await() + v2.await()

    }


    @Test
    fun `new test`() {
        runBlocking {
            try {
                assertEquals(15, multipleSuspendCalls())
            }
            catch(e: Throwable) {
                println("multipleSuspendCalls throw errpr $e")
            }
        }
    }
}