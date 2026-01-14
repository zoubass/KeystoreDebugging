package com.test.bug


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.test.bug.BiometricPromptUtils.canAuthenticate
import com.test.bug.BiometricPromptUtils.createAuthenticationCallback
import com.test.bug.BiometricPromptUtils.createBiometricPrompt
import com.test.bug.BiometricPromptUtils.findFragmentActivity


@Composable
fun BiometricPromptDialog(
    context: ComponentActivity,
    title: String,
    subTitle: String? = null,
    description: String? = null,
    cryptoObject: BiometricPrompt.CryptoObject? = null,
    onAuthenticated: (BiometricPrompt.AuthenticationResult) -> Unit,
    onFailedAuthentication: (FingerprintError) -> Unit,
    cancelOnWrongFingerprint: Boolean = false
) {
    val context = LocalContext.current
    val activity = context.findFragmentActivity() ?: throw IllegalArgumentException("Context is not a FragmentActivity")

    val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setAllowedAuthenticators(BIOMETRIC_STRONG)
        .setNegativeButtonText("cancel")

    subTitle?.let {
        promptInfoBuilder.setSubtitle(it)
    }
    description?.let {
        promptInfoBuilder.setDescription(it)
    }
    val promptInfo = promptInfoBuilder.build()

    val callback = createAuthenticationCallback(
        onAuthenticated,
        onFailedAuthentication,
        cancelOnWrongFingerprint
    )
    val biometricPrompt = createBiometricPrompt(activity, callback)

    DisposableEffect(biometricPrompt) {

        if (canAuthenticate(activity, callback)) {
            cryptoObject?.let {
                biometricPrompt.authenticate(promptInfo, it)
            } ?: biometricPrompt.authenticate(promptInfo)
        }

        onDispose {
            biometricPrompt.cancelAuthentication()
        }
    }
}

object BiometricPromptUtils {
    private const val TAG = "BiometricPromptUtils"

    fun createAuthenticationCallback(
        onAuthenticated: (BiometricPrompt.AuthenticationResult) -> Unit,
        onFailedAuthentication: (FingerprintError) -> Unit,
        cancelOnWrongFingerprint: Boolean
    ): BiometricPrompt.AuthenticationCallback {
        return object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                Log.d(TAG, "Fingerprint error. Code: $errCode, Message: $errString")
                if (errCode != BIOMETRIC_ERROR_WRONG_FINGERPRINT) {
                    onFailedAuthentication.invoke(translateErrorCode(errCode))
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                if (cancelOnWrongFingerprint) {
                    onFailedAuthentication.invoke(FingerprintError.WRONG_FINGERPRINT)
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthenticated(result)
            }
        }
    }

    fun createBiometricPrompt(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        return BiometricPrompt(activity, executor, callback)
    }

    fun canAuthenticate(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ): Boolean {
        val canAuthenticate = BiometricManager
            .from(activity)
            .canAuthenticate(BIOMETRIC_STRONG)

        return when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                callback.onAuthenticationError(BiometricPrompt.ERROR_HW_UNAVAILABLE, "")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                callback.onAuthenticationError(BiometricPrompt.ERROR_HW_NOT_PRESENT, "")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                callback.onAuthenticationError(BiometricPrompt.ERROR_NO_BIOMETRICS, "")
                false
            }

            else -> {
                callback.onAuthenticationError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, "")
                false
            }
        }
    }

    private fun translateErrorCode(errCode: Int): FingerprintError {
        return when (errCode) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> FingerprintError.MISSING_OR_NOT_AVAILABLE_HW

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> FingerprintError.NO_ENROLLED_FINGERPRINT
            BIOMETRIC_ERROR_WRONG_FINGERPRINT -> FingerprintError.WRONG_FINGERPRINT
            BIOMETRIC_ERROR_TOO_MANY_ATTEMPTS -> FingerprintError.TOO_MANY_ATTEMPTS
            BIOMETRIC_ERROR_USER_CANCELLED, BIOMETRIC_ERROR_USER_DISMISSED -> FingerprintError.CANCELED

            else -> FingerprintError.GENERAL_ERROR
        }
    }

    fun Context.findFragmentActivity(): FragmentActivity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is FragmentActivity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}

enum class FingerprintError {
    CANCELED,
    WRONG_FINGERPRINT,
    NO_ENROLLED_FINGERPRINT,
    MISSING_OR_NOT_AVAILABLE_HW,
    GENERAL_ERROR,
    BAD_FINGERPRINT,
    TOO_MANY_ATTEMPTS
}

private const val BIOMETRIC_ERROR_USER_CANCELLED = 13
private const val BIOMETRIC_ERROR_WRONG_FINGERPRINT = 5
private const val BIOMETRIC_ERROR_TOO_MANY_ATTEMPTS = 7
private const val BIOMETRIC_ERROR_USER_DISMISSED = 10
