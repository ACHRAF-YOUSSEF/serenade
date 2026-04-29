package com.serenade.app.feature.auth.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureTokenStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String?) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, encrypt(accessToken))

            if (refreshToken == null) {
                remove(KEY_REFRESH_TOKEN)
            } else {
                putString(KEY_REFRESH_TOKEN, encrypt(refreshToken))
            }
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)?.decryptOrNull()

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)?.decryptOrNull()

    fun clear() {
        prefs.edit { clear() }
    }

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val ciphertext = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val iv = cipher.iv
        val payload = byteArrayOf(iv.size.toByte()) + iv + ciphertext
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun String.decryptOrNull(): String? {
        return try {
            val payload = Base64.decode(this, Base64.NO_WRAP)
            if (payload.isEmpty()) return null

            val ivSize = payload.first().toInt()
            if (ivSize <= 0 || payload.size <= IV_SIZE_PREFIX_BYTES + ivSize) return null

            val iv = payload.copyOfRange(IV_SIZE_PREFIX_BYTES, IV_SIZE_PREFIX_BYTES + ivSize)
            val ciphertext = payload.copyOfRange(IV_SIZE_PREFIX_BYTES + ivSize, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))

            String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
        } catch (_: GeneralSecurityException) {
            null
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let {
            return it
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val FILE_NAME = "serenade_tokens"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "serenade_token_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE_PREFIX_BYTES = 1
        const val GCM_TAG_BITS = 128
    }
}
