package com.nuvio.app.features.cinemaos

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class CinemaOsStoredSession(
    val label: String,
    val uid: String,
    val refreshToken: String,
    val idToken: String,
    val idTokenExpiresAtMs: Long,
)

/** Persists the CinemaOS session with values encrypted by an Android Keystore AES-GCM key. */
internal class CinemaOsAccountStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("cinemaos_account", Context.MODE_PRIVATE)

    fun load(): CinemaOsStoredSession? = runCatching {
        val label = prefs.getString(K_EMAIL, null)?.let(::decrypt) ?: return null
        CinemaOsStoredSession(
            label = label,
            uid = prefs.getString(K_UID, null)?.let(::decrypt) ?: return null,
            refreshToken = prefs.getString(K_REFRESH, null)?.let(::decrypt) ?: return null,
            idToken = prefs.getString(K_ID, null)?.let(::decrypt) ?: "",
            idTokenExpiresAtMs = prefs.getLong(K_EXP, 0L),
        )
    }.getOrNull()

    fun save(session: CinemaOsStoredSession) {
        runCatching {
            prefs.edit()
                .putString(K_EMAIL, encrypt(session.label))
                .putString(K_UID, encrypt(session.uid))
                .putString(K_REFRESH, encrypt(session.refreshToken))
                .putString(K_ID, encrypt(session.idToken))
                .putLong(K_EXP, session.idTokenExpiresAtMs)
                .apply()
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return gen.generateKey()
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val out = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(out, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val raw = Base64.decode(encoded, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, raw, 0, IV_LEN))
        return String(cipher.doFinal(raw, IV_LEN, raw.size - IV_LEN), Charsets.UTF_8)
    }

    private companion object {
        const val ALIAS = "cinemaos_account_key"
        const val TRANSFORM = "AES/GCM/NoPadding"
        const val IV_LEN = 12
        const val K_EMAIL = "email"
        const val K_UID = "uid"
        const val K_REFRESH = "refresh"
        const val K_ID = "id"
        const val K_EXP = "exp"
    }
}
