package isel.leic.pc.lec_05_23

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.coroutines.CoroutineContext

class JobsTests {
    private suspend fun getValue(value: Int) : Int {
        delay(6000)
        return value
    }

    @Test
    fun `show that runBlocking waits for all coroutine(jobs) childs`() {
        runBlocking {
            val job1 = launch {
                getValue(5)
            }

            val job2 = launch {
                getValue(3)
            }

            // show the parent job
            coroutineContext.job.show()
            println()

            // show child 1
            job1.show()
            println()

            // show child 2
            job2.show()
            println()

            println("all jobs done")
        }
        println("runBlocking done")
    }


    @Test
    fun `show that a Job without a coroutine must be completed programatically`() {
        runBlocking {

            val parentJob = Job()

            val name1 = CoroutineName("child1")
            val context1 : CoroutineContext = parentJob + name1
            val job1 = launch(context1) {
                println("coroutine name : ${coroutineContext[CoroutineName]?.name}")
                getValue(5)
                println("child1 terminate")
            }
            val job2 = launch(parentJob + CoroutineName("child2")) {
                println("coroutine name : ${coroutineContext[CoroutineName]?.name}")
                getValue(3)
                println("child2 terminate")
            }

            coroutineContext.job.show()
            println()
            parentJob.show()

            println()

            job1.show()

            println()
            job2.show()
            println()

            launch {
                repeat(10) {
                    delay(1000)
                    println("parentJob state = ${parentJob.state}")
                }
                parentJob.complete()
            }

            // parentJob must be completed to terminate join suspension
            parentJob.join()
            println("all jobs done")
        }
        println("runBlocking done")
    }

}