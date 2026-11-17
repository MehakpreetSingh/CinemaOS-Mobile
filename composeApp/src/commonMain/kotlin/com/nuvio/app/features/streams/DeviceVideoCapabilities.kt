package com.nuvio.app.features.streams

/** What this device can decode / show: display max height and HEVC / AV1 / H.264 4K support. */
data class DeviceVideoCaps(
    val maxDisplayHeight: Int,
    val hevc: Boolean,
    val hevc4k: Boolean,
    val hevc10bit: Boolean,
    val av1: Boolean,
    val av1_4k: Boolean,
    val h264_4k: Boolean,
)

expect object DeviceVideoCapabilities {
    fun caps(): DeviceVideoCaps
}
