package com.orange.ccmd.sandbox.models

data class SegmentData(val id: String, val name: String, val efforts: MutableMap<String, Float>) {

    // 🤮
    private fun percentile(time: Float): Float {
        val sorted = efforts.values.sorted()
        val l = sorted.size
        var i = 0

        for (effort in sorted) {
            if (time <= effort) {

                while (i < l && time == sorted[i]) i++

                if (i == 0) return 0.toFloat()

                var iFloat = i.toFloat()
                if (time != sorted[i - 1]) {
                    iFloat = iFloat.plus((time.minus(sorted[i - 1])).div(sorted[i].minus(sorted[i - 1])))
                }
                return iFloat.div(l)
            }
            i++
        }
        return 1.toFloat()
    }

    fun roundedPercentile(time: Float) = Math.round(percentile(time).times(100)).toFloat().div(100)
}
