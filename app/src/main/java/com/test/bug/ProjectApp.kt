package com.test.bug

import android.app.Application
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.i("ProjectApp", "Application started")
        KeyStoreUtils.generateEcKeyStore(PER_OP_KEY_ALIAS, true)
        KeyStoreUtils.generateEcKeyStore(TIME_BASED_KEY_ALIAS, true, 5)

    }

    companion object {
        const val PER_OP_KEY_ALIAS = "signing-per-op"
        const val TIME_BASED_KEY_ALIAS = "signing-time-based"
        lateinit var instance: ProjectApp
    }
}