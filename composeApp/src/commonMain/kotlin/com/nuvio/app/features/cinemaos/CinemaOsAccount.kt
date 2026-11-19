package com.nuvio.app.features.cinemaos

import androidx.compose.ui.graphics.ImageBitmap
import com.nuvio.app.features.library.LibraryItem
import com.nuvio.app.features.watchprogress.WatchProgressEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Signed-in cinemaos.tech account (Firebase user). [label] is the email or display name. */
data class CinemaOsAccountSession(
    val label: String,
    val uid: String,
    val displayName: String? = null,
)

data class CinemaOsDeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationUrl: String,
    val verificationUrlComplete: String,
    val expiresInSec: Int,
    val intervalSec: Int,
)

sealed interface CinemaOsPollResult {
    data object Pending : CinemaOsPollResult
    data object SlowDown : CinemaOsPollResult
    data object Expired : CinemaOsPollResult
    data object Denied : CinemaOsPollResult
    data class Approved(val customToken: String) : CinemaOsPollResult
}

/**
 * CinemaOS website account + watch history / watchlist sync.
 * Android provides the real implementation; other platforms use [NoOpCinemaOsAccountService].
 */
interface CinemaOsAccountService {
    val isSupported: Boolean
    val session: StateFlow<CinemaOsAccountSession?>
    val isSyncing: StateFlow<Boolean>
    val lastSyncedAt: StateFlow<Long>

    suspend fun startDeviceLogin(): Result<CinemaOsDeviceCode>
    suspend fun pollDeviceLogin(deviceCode: String): Result<CinemaOsPollResult>
    suspend fun completeSignIn(customToken: String): Result<Unit>
    fun signOut()
    fun syncNow()

    /** QR code bitmap for [content], or null when unsupported. */
    fun qrCode(content: String): ImageBitmap?

    // Hooks called from the local repositories (no-ops while signed out).
    fun onProgressSaved(entry: WatchProgressEntry)
    fun onProgressRemoved(entries: Collection<WatchProgressEntry>)
    fun onLibraryChanged(item: LibraryItem, added: Boolean)
}

object NoOpCinemaOsAccountService : CinemaOsAccountService {
    override val isSupported: Boolean = false
    override val session: StateFlow<CinemaOsAccountSession?> = MutableStateFlow(null)
    override val isSyncing: StateFlow<Boolean> = MutableStateFlow(false)
    override val lastSyncedAt: StateFlow<Long> = MutableStateFlow(0L)

    override suspend fun startDeviceLogin(): Result<CinemaOsDeviceCode> =
        Result.failure(UnsupportedOperationException("CinemaOS account is not supported on this platform"))

    override suspend fun pollDeviceLogin(deviceCode: String): Result<CinemaOsPollResult> =
        Result.failure(UnsupportedOperationException("CinemaOS account is not supported on this platform"))

    override suspend fun completeSignIn(customToken: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("CinemaOS account is not supported on this platform"))

    override fun signOut() = Unit
    override fun syncNow() = Unit
    override fun qrCode(content: String): ImageBitmap? = null
    override fun onProgressSaved(entry: WatchProgressEntry) = Unit
    override fun onProgressRemoved(entries: Collection<WatchProgressEntry>) = Unit
    override fun onLibraryChanged(item: LibraryItem, added: Boolean) = Unit
}

object CinemaOsAccount {
    /** Replaced at startup by the Android application; never reassigned afterwards. */
    var service: CinemaOsAccountService = NoOpCinemaOsAccountService
}
