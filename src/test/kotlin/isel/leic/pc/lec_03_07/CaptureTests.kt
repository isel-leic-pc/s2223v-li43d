package isel.leic.pc.lec_03_06

import org.junit.Test
import kotlin.concurrent.thread

class CaptureTests {

    @Test
    fun `observe different effets on captured variables by multiple threads`() {
        val threads = mutableListOf<Thread>()
        var num = 0

        repeat(10) {
            num++
            val localnum = num
            thread {
                println(it)
            }.apply { threads.add(this) }
        }

        threads.forEach {
            it.join()
        }
    }
}