package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StreamInfoParserTest {
    @Test
    fun releaseKeepsSeparateAudioAndSourceBadges() {
        val info = parseStreamInfo(listOf(
            "Movie.2160p.BluRay.REMUX.DoVi.HDR10Plus.HEVC.10-BIT.TrueHD.Atmos.DTS-HD.MA.7.1",
            "24.5 GB [Hin-Eng]",
        ))
        assertEquals("4K", info.qualityLabel)
        assertEquals("24.5 GB", info.sizeLabel)
        assertTrue(info.dolbyVision)
        assertTrue(info.hdr10Plus)
        assertFalse(info.hdr10)
        assertFalse(info.hdr)
        assertTrue(info.atmos)
        assertEquals(setOf("DTS-HD", "TrueHD"), info.audioFormats.toSet())
        assertEquals(listOf("REMUX", "BluRay"), info.sourceTypes)
        assertEquals("7.1", info.channels)
        assertEquals("HEVC", info.codec)
        assertTrue(info.tenBit)
        assertEquals(listOf("Hindi", "English"), info.languages.map { it.name })
    }

    @Test
    fun dolbyAndChannelAliasesProduceTheSameBadges() {
        for (alias in listOf("DDP5.1", "DD+5.1", "EAC3 6ch", "E-AC-3 6ch", "Dolby Digital+ 6ch", "Digital Plus 6ch")) {
            val info = parseStreamInfo(listOf(alias))
            assertEquals(listOf("DD+"), info.audioFormats, alias)
            assertEquals("5.1", info.channels, alias)
        }
        assertEquals("7.1", parseStreamInfo(listOf("8ch")).channels)
        for (alias in listOf("Dolby Vision", "DoVi", "DV")) {
            assertTrue(parseStreamInfo(listOf(alias)).dolbyVision, alias)
        }
        for (alias in listOf("HDR10+", "HDR10Plus", "HDR 10 Plus")) {
            val info = parseStreamInfo(listOf(alias))
            assertTrue(info.hdr10Plus, alias)
            assertFalse(info.hdr10, alias)
        }
    }

    @Test
    fun dtsFamilyIsDistinctAndAliasesDoNotDuplicateBadges() {
        assertEquals(listOf("DTS:X"), parseStreamInfo(listOf("DTS:X DTS-X DTSX")).audioFormats)
        assertEquals(listOf("DTS-HD"), parseStreamInfo(listOf("DTS-HD MA DTS.HD")).audioFormats)
        assertEquals(listOf("DTS"), parseStreamInfo(listOf("DTS")).audioFormats)
        assertEquals(listOf("DD+"), parseStreamInfo(listOf("DDP EAC3 DD+")).audioFormats)
    }

    @Test
    fun plainHdrAndWebCodecsRemainAvailable() {
        val info = parseStreamInfo(listOf("Show 1080p WEB-DL HDR IMAX AV1 10bit English"))
        assertEquals("1080p", info.qualityLabel)
        assertEquals(listOf("WEB-DL"), info.sourceTypes)
        assertTrue(info.hdr)
        assertTrue(info.imax)
        assertEquals("AV1", info.codec)
        assertTrue(info.tenBit)
        assertEquals("720p", parseStreamInfo(listOf("720p HDR10")).qualityLabel)
        assertTrue(parseStreamInfo(listOf("720p HDR10")).hdr10)
        assertFalse(parseStreamInfo(listOf("Address Digitalization DDPower")).audioFormats.isNotEmpty())
    }
}
