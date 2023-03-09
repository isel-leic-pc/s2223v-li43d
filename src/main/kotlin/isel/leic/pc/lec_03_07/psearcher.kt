package isel.leic.pc.lec_03_06

fun search(values: Array<String>, ref: String) : Int {
    var total = 0
    for (i in 0 until values.size)
        if (ref.equals(values[i]))
            total++
    return total
}