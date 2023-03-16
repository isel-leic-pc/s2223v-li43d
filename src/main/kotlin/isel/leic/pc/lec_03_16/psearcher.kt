package isel.leic.pc.lec_03_16


fun search_seq1(values: Array<String>, ref: String) : Int {
    var total = 0
    for (i in 0 until values.size)
        if (ref.equals(values[i]))
            total++
    return total
}

fun search_seq2(values: Array<String>, ref: String) : Int {
    return values.filter {
        ref.equals(it)
    }
    .size
}

fun psearch(values: Array<String>, ref: String) : Int  {
   return 0
}