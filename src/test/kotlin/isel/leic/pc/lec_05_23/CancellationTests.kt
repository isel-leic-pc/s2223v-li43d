package isel.leic.pc.lec_05_23

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test


class CancellationTests {

    @Test
    fun `cancellation of a child of a coroutine with multiple childs don't propagate to parent`() {
        runBlocking {

            val parentJob = Job()

            var exception1 : Throwable? = null

            val child1 = launch(parentJob) {
                // this child will be cancelled too
                launch {
                    try {
                        delay(3000)
                        println("this message doesn't appear")
                    }
                    catch(e: CancellationException) {
                        exception1 = e
                    }
                }
                delay(1000)

                println("child1 done!")

            }

            var exception2 : Throwable? = null

            launch(parentJob) {
                try {
                    delay(2000)
                    println("child2 done!")

                }
                catch(e: Throwable) {
                    println("child2 terminate with error '${e.message}'")
                    exception2 = e
                }

            }

            delay(500)
            child1.cancel()

            parentJob.complete()
            parentJob.join()

            Assert.assertTrue(exception1 is CancellationException)
            Assert.assertTrue(exception2 == null)
            Assert.assertTrue(parentJob.isCompleted)
        }

        println("done!")
    }
}