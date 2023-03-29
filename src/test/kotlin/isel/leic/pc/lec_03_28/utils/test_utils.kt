package isel.leic.pc.lec_03_28.utils

import kotlin.concurrent.thread

// this test auxiliary function launches a given number of threads
// that will execute the given code block
// Each thread have a different index and a friendly name, that
// can be useful for debug
fun launchMany( total : Int, tName : String = "thread", block : (i: Int) -> Unit) : List<Thread> {
    return  (0 until total)
        .map {
            thread(name = tName + "_" + it) {
                block(it)
            }
        }
}