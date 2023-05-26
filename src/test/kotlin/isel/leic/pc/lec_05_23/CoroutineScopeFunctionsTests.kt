package isel.leic.pc.lec_05_23

import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.internal.runners.statements.ExpectException
import kotlin.coroutines.coroutineContext

class CoroutineScopeFunctionsTests {

    suspend fun getValue(v: Int) : Int {
        delay(1000)
        println("$coroutineContext produce $v")
        return v
    }

    private suspend fun getSumValues() : Int = coroutineScope {
       TODO()
    }

    private suspend fun getSumValuesWithError() : Int = supervisorScope {
      TODO()
    }

    @Test
    fun `coroutineScope with multiple childs execution`() {

    }

    @Test(expected = Exception::class)
    fun `coroutineScope with multiple childs execution with error`() {

    }

}