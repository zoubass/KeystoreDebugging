package com.test.bug

import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.security.KeyPairGenerator

object KeyStoreUtils {

    private const val EC_KEY_SIZE_BITS: Int = 256
    private const val AUTHENTICATION_TIMEOUT_R_AND_LATER = 0
    const val ANDROID_KEYSTORE = "AndroidKeyStore"

    private const val USE_STRONGBOX_IF_AVAILABLE = true

    /**
     * Generates EC keypair for the purpose of SIGN. Default key size in bits is specified in *EC_KEY_SIZE_BITS*.
     *
     * @param keyAlias alias of the entry in which the generated key will appear in Android KeyStore. Must not be empty
     * @param authenticationRequired whether this key is authorized to be used only if the user has been authenticated.
     * @param authenticationTimeoutSeconds duration in seconds or 0 if user authentication must take place for every use of the key. The default is 0.
     */
    fun generateEcKeyStore(
        keyAlias: String,
        authenticationRequired: Boolean,
        authenticationTimeoutSeconds: Int = AUTHENTICATION_TIMEOUT_R_AND_LATER,
        signatureDigest: String = KeyProperties.DIGEST_SHA256,
        attestationChallenge: ByteArray? = null
    ) {
        generateKeystore(
            KeyProperties.KEY_ALGORITHM_EC,
            EC_KEY_SIZE_BITS,
            KeyProperties.PURPOSE_SIGN,
            keyAlias,
            authenticationRequired,
            authenticationTimeoutSeconds,
            signatureDigest,
            attestationChallenge = attestationChallenge
        )

        Log.i("KeyStoreUtils", "EC key generated with alias: $keyAlias")
    }

    private fun generateKeystore(
        keyAlgorithm: String,
        keySize: Int,
        keyPurpose: Int,
        keyAlias: String,
        authenticationRequired: Boolean,
        authenticationTimeoutSeconds: Int,
        signatureDigest: String? = KeyProperties.DIGEST_SHA256,
        attestationChallenge: ByteArray? = null
    ) {
        val projectInstance = ProjectApp.instance

        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            keyAlgorithm,
            ANDROID_KEYSTORE
        )
        val keyBuilder = KeyGenParameterSpec.Builder(
            keyAlias, keyPurpose
        ).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setUserAuthenticationRequired(authenticationRequired)
            .setUserAuthenticationValidity(authenticationTimeoutSeconds)
            .setDigests(signatureDigest, KeyProperties.DIGEST_NONE)
            .setKeySize(keySize)

        if (attestationChallenge != null) {
            keyBuilder.setAttestationChallenge(attestationChallenge)
        }

        if (projectInstance.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)) {
            keyBuilder.setIsStrongBoxBacked(USE_STRONGBOX_IF_AVAILABLE)
            Log.i("KeyStoreUtils", "StrongBox supported.")
        } else {
            Log.i("KeyStoreUtils", "StrongBox not supported on this device")
        }

        with(kpg) {
            initialize(keyBuilder.build())
            generateKeyPair()
        }
    }
}

/**
 * Extension function of KeyGenParameterSpec.Builder to set keys validity duration considering SDK version.
 *
 * @param authorizationTimeout key validity duration in seconds.
 */
private fun KeyGenParameterSpec.Builder.setUserAuthenticationValidity(authorizationTimeout: Int): KeyGenParameterSpec.Builder {
    val authTimeout = if (authorizationTimeout < 0) 0 else authorizationTimeout
    setUserAuthenticationParameters(authTimeout, KeyProperties.AUTH_BIOMETRIC_STRONG)
    return this
}
