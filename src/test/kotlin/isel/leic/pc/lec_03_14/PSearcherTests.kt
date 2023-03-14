package isel.leic.pc.lec_03_14

import org.junit.Test
import kotlin.system.measureNanoTime
import kotlin.test.assertEquals

class PSearcherTests {
    private fun buildString() : String {
        val sb = StringBuilder()

        repeat(50000) {
            sb.append('a' + (it % 24))
        }

        return sb.toString()
    }

    private fun test( function : (Array<String>, String) -> Int,
                      values: Array<String>,
                      ref: String, prefix: String) : Pair<Int,Long>{
        var minTime = Long.MAX_VALUE
        var total = 0
        repeat(5) {
            var curTotal : Int
            val time = measureNanoTime{
                curTotal =  function(values, ref)
            }
            if (time < minTime) {
                minTime = time
                total = curTotal
            }
        }
        return Pair(total, minTime)
    }

    @Test
    fun `test sequencial versus parallel search times`() {
        val NSTRINGS = 1000_000
        val s = buildString()
        val values = Array<String>(NSTRINGS) { s }

        val (total_seq, time_seq) =
            test(::search, values, buildString(), "serial")
        val (total_par, time_par) =
            test(::psearch, values, buildString(), "parallel")

        assertEquals(NSTRINGS,total_seq )
        assertEquals(NSTRINGS,total_par )
        println("seq search in ${time_seq} nanos!")
        println("par search in ${time_par} nanos!")


    }
}