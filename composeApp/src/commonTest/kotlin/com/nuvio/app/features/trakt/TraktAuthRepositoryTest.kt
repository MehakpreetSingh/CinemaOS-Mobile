package com.nuvio.app.features.trakt

import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals

class TraktAuthRepositoryTest {

    @Test
    fun `authorization uses RFC 7636 S256 challenge and preserves callback parameters`() {
        val callback = "https://cinemaos.live/auth/trakt/callback"
        val url = Url(traktAuthorizationUrl("test-client", callback, "state+value&", "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"))
        assertEquals("auth.trakt.tv", url.host)
        assertEquals(callback, url.parameters["redirect_uri"])
        assertEquals("state+value&", url.parameters["state"])
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", url.parameters["code_challenge"])
        assertEquals("S256", url.parameters["code_challenge_method"])
        assertEquals(null, url.parameters["code_verifier"])
        assertEquals(null, url.parameters["client_secret"])
    }

    @Test
    fun `HTTP 400 permanently invalidates a rejected refresh token`() {
        assertEquals(
            TraktTokenRefreshResponseAction.INVALIDATE,
            traktTokenRefreshResponseAction(400),
        )
    }

    @Test
    fun `successful token responses are accepted`() {
        assertEquals(
            TraktTokenRefreshResponseAction.ACCEPT,
            traktTokenRefreshResponseAction(200),
        )
        assertEquals(
            TraktTokenRefreshResponseAction.ACCEPT,
            traktTokenRefreshResponseAction(201),
        )
    }

    @Test
    fun `non-400 failures preserve credentials for a later attempt`() {
        listOf(401, 429, 500, 503).forEach { status ->
            assertEquals(
                TraktTokenRefreshResponseAction.TRANSIENT_FAILURE,
                traktTokenRefreshResponseAction(status),
            )
        }
    }
}
