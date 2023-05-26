package isel.leic.pc.lec_05_23

import kotlinx.coroutines.*
import org.junit.Assert.assertTrue
import org.junit.Test

class ExceptionsTests {

    @Test
    fun `non catched exception on a child of a coroutine with multiple childs terminate all`() {
        runBlocking {

            val parentJob = Job()
            launch(parentJob) {
                delay(1000)
                throw Error("simulated error")
                println("child1 done!")

            }

            var exception : Throwable? = null

            launch(parentJob) {
                try {
                    delay(2000)
                    println("child2 done!")

                }
                catch(e: Throwable) {
                    println("child2 terminate with error '${e.message}'")
                    exception = e
                }

            }
            parentJob.complete()
            parentJob.join()

            assertTrue(exception is CancellationException)
            assertTrue(parentJob.isCancelled)
        }

        println("done!")
    }

}