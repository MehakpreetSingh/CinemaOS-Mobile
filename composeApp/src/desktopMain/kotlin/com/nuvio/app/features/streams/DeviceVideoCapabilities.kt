package com.nuvio.app.features.streams

actual object DeviceVideoCapabilities {
    actual fun caps(): DeviceVideoCaps = DeviceVideoCaps(
        maxDisplayHeight = 2160,
        hevc = true,
        hevc4k = true,
        hevc10bit = true,
        av1 = false,
        av1_4k = false,
        h264_4k = true,
    )
}
