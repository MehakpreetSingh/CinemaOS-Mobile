package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StreamingBufferProfileTest {
    @Test
    fun lowRamFlagOverridesReportedPhysicalRam() {
        val constrained = StreamingBufferProfile.forDevice(16_384, true)
        val unknown = StreamingBufferProfile.forDevice(0, false)
        assertTrue(constrained.lowMemory)
        assertEquals(unknown, constrained)
        assertTrue(constrained.forwardMb + constrained.backMb <= 48)
    }

    @Test
    fun budgetsGrowWithinBoundsAndFavorForwardPlayback() {
        val profiles = listOf(1_024L, 2_048L, 4_096L, 6_144L, 16_384L)
            .map { StreamingBufferProfile.forDevice(it, false) }
        assertEquals(profiles.map { it.forwardMb }.sorted(), profiles.map { it.forwardMb })
        for (profile in profiles) {
            assertTrue(profile.backMb > 0)
            assertTrue(profile.forwardMb >= profile.backMb * 4)
            assertTrue(profile.forwardMb + profile.backMb <= 224)
        }
    }
}
