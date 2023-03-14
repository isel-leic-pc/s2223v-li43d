package isel.leic.pc.lec_03_14


import kotlin.concurrent.thread
import kotlin.math.min

fun search(values: Array<String>, ref: String) : Int {
    var total = 0
    for (i in 0 until values.size)
        if (ref.equals(values[i]))
            total++
    return total
}

fun psearch(values: Array<String>, ref: String) : Int  {
    val NPROCS = Runtime.getRuntime().availableProcessors()
    val partials = IntArray(NPROCS)

    val threads = ArrayList<Thread>()

    val slotSize = values.size / NPROCS
    var start = 0
    var count = 0;
    repeat(NPROCS) {
        val localStart  = start
        val localEnd =  min(localStart + slotSize, values.size)
        start = localEnd
        val t = thread {
            var partialCount = 0
            for(i in localStart until localEnd) {
                if (values[i].equals(ref))
                    //partialCount++
                    //partials[it]++
                    count++
            }
            //partials[it] = partialCount
        }

        threads.add(t)
    }

    threads.forEach {
        it.join()
    }

    return values.size
    //return partials.sum()
}