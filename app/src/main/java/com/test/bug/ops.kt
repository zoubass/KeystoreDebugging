package com.test.bug

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature


fun sign(keyAlias: String, data: ByteArray?): ByteArray? {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val signature = getSignature(keyAlias)
            sign(signature, data)
            Log.i("Crypto", "Signed - timebased")
        } catch (e: Exception) {
            Log.e("Crypto", "Signing failed", e)
            throw e
        }
    }
    return null

}

fun sign(
    signature: Signature,
    data: ByteArray?,
): ByteArray? {
    try {
        signature.update(data)
        return signature.sign()
    } catch (e: Exception) {
        Log.e("TEST", "Signing failed", e)
        throw e
    }
}

fun getSignature(alias: String): Signature {
    val keystore = KeyStore.getInstance("AndroidKeyStore")
    keystore.load(null)
    val prv = keystore.getKey(alias, null)

    val signature = Signature.getInstance("SHA256withECDSA")
    signature.initSign(prv as PrivateKey)
    return signature
}