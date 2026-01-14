package com.test.bug

import android.util.Log
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature

fun sign(keyAlias: String, data: ByteArray?): ByteArray? {
    return try {
        val signature = getSignature(keyAlias)
        sign(signature, data)
    } catch (e: Exception) {
        Log.e("Crypto", "Signing failed", e)
        throw e
    }
}

fun sign(
    signature: Signature,
    data: ByteArray?,
): ByteArray? {
    return try {
        signature.update(data)
        signature.sign()
    } catch (e: Exception) {
        Log.e("Crypto", "Signing failed", e)
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