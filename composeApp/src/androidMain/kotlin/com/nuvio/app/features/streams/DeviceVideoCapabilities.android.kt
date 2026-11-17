package com.nuvio.app.features.streams

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.display.DisplayManager
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.view.Display

/** Detected lazily, once: display max resolution and the decoders for HEVC / AV1 / H.264. */
actual object DeviceVideoCapabilities {
    private val fallback = DeviceVideoCaps(
        maxDisplayHeight = 1080,
        hevc = true,
        hevc4k = false,
        hevc10bit = false,
        av1 = false,
        av1_4k = false,
        h264_4k = false,
    )

    @Volatile
    private var cached: DeviceVideoCaps? = null

    actual fun caps(): DeviceVideoCaps = cached ?: synchronized(this) { cached ?: detect().also { cached = it } }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun appContext(): Context? = runCatching {
        val activityThread = Class.forName("android.app.ActivityThread")
        activityThread.getMethod("currentApplication").invoke(null) as? Application
    }.getOrNull()

    private fun detect(): DeviceVideoCaps {
        val context = appContext() ?: return fallback
        return runCatching {
            DeviceVideoCaps(
                maxDisplayHeight = detectDisplayHeight(context),
                hevc = hasDecoder("video/hevc"),
                hevc4k = hasDecoder("video/hevc", 3840, 2160),
                hevc10bit = hasHevcMain10(),
                av1 = hasDecoder("video/av01"),
                av1_4k = hasDecoder("video/av01", 3840, 2160),
                h264_4k = hasDecoder("video/avc", 3840, 2160),
            )
        }.getOrDefault(fallback)
    }

    private fun detectDisplayHeight(context: Context): Int {
        val manager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return 1080
        val display = manager.getDisplay(Display.DEFAULT_DISPLAY) ?: return 1080
        var best = 0
        for (mode in display.supportedModes) {
            val long = maxOf(mode.physicalWidth, mode.physicalHeight)
            val short = minOf(mode.physicalWidth, mode.physicalHeight)
            val tier = when {
                long >= 3800 || short >= 2100 -> 2160
                long >= 2500 || short >= 1400 -> 1440
                long >= 1900 || short >= 1000 -> 1080
                else -> 720
            }
            if (tier > best) best = tier
        }
        return if (best == 0) 1080 else best
    }

    private fun decoders(mime: String): List<MediaCodecInfo> =
        MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.filter { info ->
            !info.isEncoder && info.supportedTypes.any { it.equals(mime, ignoreCase = true) }
        }

    private fun hasDecoder(mime: String, width: Int = 0, height: Int = 0): Boolean = decoders(mime).any { info ->
        if (width == 0) {
            true
        } else {
            runCatching {
                info.getCapabilitiesForType(mime).videoCapabilities?.isSizeSupported(width, height) == true
            }.getOrDefault(false)
        }
    }

    private fun hasHevcMain10(): Boolean = decoders("video/hevc").any { info ->
        runCatching {
            info.getCapabilitiesForType("video/hevc").profileLevels.any {
                it.profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10 ||
                    it.profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10
            }
        }.getOrDefault(false)
    }
}
