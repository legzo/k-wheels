package com.orange.ccmd.sandbox.models

data class SegmentData(val id: String, val name: String, val efforts: MutableMap<String, Number>) {

    // 🤮
    private fun percentile(time: Number): Float {
        val sorted = efforts.values.map { it.toFloat() }.toFloatArray().sorted()
        val v = time.toFloat()
        val l = sorted.size
        var i = 0

        for (effort in sorted) {
            if (v <= effort) {

                while (i < l && v == sorted[i]) i++

                if (i == 0) return 0.toFloat()

                var iFloat = i.toFloat()
                if (v != sorted[i - 1]) {
                    iFloat = iFloat.plus((v.minus(sorted[i - 1])).div(sorted[i].minus(sorted[i - 1])))
                }
                return iFloat.div(l)
            }
            i++
        }
        return 1.toFloat()
    }

    fun roundedPercentile(time: Number) = Math.round(percentile(time).times(100)).toFloat().div(100)
}
