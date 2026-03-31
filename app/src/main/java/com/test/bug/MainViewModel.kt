package com.test.bug

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.test.bug.ProjectApp.Companion.PER_OP_KEY_ALIAS
import com.test.bug.ProjectApp.Companion.TIME_BASED_KEY_ALIAS
import java.security.SecureRandom

class MainViewModel : ViewModel() {

    val showDialog = mutableStateOf(false)
    val statusText = mutableStateOf("")
    var currentDataSize: DataSize = DataSize.DATA_256

    fun signPerOp(
        it: BiometricPrompt.AuthenticationResult
    ) {
        val message = generateRandomString(currentDataSize.size).toByteArray()
        Log.d(TAG, "Signing per operation auth ${message.size} bytes")
        showDialog.value = false
        statusText.value = ""
        try {
            val signature = it.cryptoObject!!.signature!!
            signature.update(message)
            val result = signature.sign()
            statusText.value = "Signed result: ${result.size}."
        } catch (e: Exception) {
            Log.e(TAG, "Signing failed", e)
            statusText.value = "failed: ${e.message}"
        }
    }

    fun signTimeBased(
        dataSize: DataSize
    ) {
        val message = generateRandomString(dataSize.size).toByteArray()
        Log.d(TAG, "Signing time based auth ${message.size}")
        showDialog.value = false
        statusText.value = ""
        try {
            val signature = getSignature(TIME_BASED_KEY_ALIAS)
            sign(signature, message)
            Log.i("Crypto", "Signed - time based")
            statusText.value = "Signed result: ${signature.sign().size}."
        } catch (e: Exception) {
            Log.e("Crypto", "Signing failed", e)
            statusText.value = "failed: ${e.message}"
            throw e
        }
    }

    fun onButtonClick(size: DataSize, key: String) {
        currentDataSize = size
        showDialog.value = true
    }

    fun onFailed() {
        showDialog.value = false
        Log.d(TAG, "Signing failed")
    }

    fun generateRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = SecureRandom()
        return (1..length)
            .map { charset[random.nextInt(charset.length)] }
            .joinToString("")
    }
    
    companion object {
        private const val TAG = "MainViewModel"
    }


    enum class DataSize(val size: Int) {
        DATA_256(256),
        DATA_257(257)
    }
}