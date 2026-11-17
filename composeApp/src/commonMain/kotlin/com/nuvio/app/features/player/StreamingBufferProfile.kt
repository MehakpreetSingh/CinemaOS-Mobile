package com.nuvio.app.features.player

/** Packet cache budgets, separate from decoder and UI memory. Unknown devices stay conservative. */
internal data class StreamingBufferProfile(
    val forwardMb: Int,
    val backMb: Int,
    val lowMemory: Boolean,
) {
    companion object {
        fun forDevice(ramMb: Long, lowRamDevice: Boolean): StreamingBufferProfile = when {
            lowRamDevice || ramMb <= 2_048L -> StreamingBufferProfile(32, 8, true)
            ramMb <= 4_096L -> StreamingBufferProfile(64, 16, false)
            ramMb <= 6_144L -> StreamingBufferProfile(128, 24, false)
            else -> StreamingBufferProfile(192, 32, false)
        }
    }
}
