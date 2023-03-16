package isel.leic.pc.lec_03_14

import isel.leic.pc.lec_03_16.psearch
import isel.leic.pc.lec_03_16.search_seq1
import isel.leic.pc.lec_03_16.search_seq2

import org.junit.Test
import kotlin.system.measureNanoTime
import kotlin.test.assertEquals

class PSearcherTests {
    private fun buildString() : String {
        val sb = StringBuilder()

        repeat(500) {
            sb.append('a' + (it % 24))
        }

        return sb.toString()
    }

    private fun test( function : (Array<String>, String) -> Int,
                      values: Array<String>,
                      ref: String) : Pair<Int,Long>{
        var minTime = Long.MAX_VALUE
        var total = 0
        repeat(1) {
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
        val NSTRINGS = 10000_000
        val s = buildString()
        val values = Array<String>(NSTRINGS) { s }

        val (total_seq1, time_seq1) =  test(::search_seq1, values, s)

        val (total_seq2, time_seq2) = test(::search_seq2, values, s)

        val (total_par, time_par) =  test(::psearch, values, s)

        assertEquals(NSTRINGS,total_seq1 )
        assertEquals(NSTRINGS,total_seq2 )
        //assertEquals(NSTRINGS,total_par )

        println("seq search in ${time_seq1} nanos!")
        println("seq search in ${time_seq2} nanos!")
        println("par search in ${time_par} nanos!")


    }
}