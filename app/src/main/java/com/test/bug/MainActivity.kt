package com.test.bug

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.test.bug.ProjectApp.Companion.PER_OP_KEY_ALIAS
import com.test.bug.ProjectApp.Companion.TIME_BASED_KEY_ALIAS
import com.test.bug.ui.theme.MotorolaKeystoreTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotorolaKeystoreTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ActionButtons(
                        activity = this,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(modifier: Modifier = Modifier, activity: MainActivity) {
    var statusText by remember { mutableStateOf("") }
    var showPerOpDialog by remember { mutableStateOf(false) }
    var showTimeBasedDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Choose an action")
        Button(onClick = {
            showPerOpDialog = true
        }) {
            Text("Per-Op")
        }
        Button(onClick = { showTimeBasedDialog = true }) {
            Text(text = "Time Based")
        }
        Text(statusText)
    }

    if (showPerOpDialog) {
        showTimeBasedDialog = false
        statusText = ""
        val signature = getSignature(PER_OP_KEY_ALIAS)

        BiometricPromptDialog(
            context = activity,
            cryptoObject = BiometricPrompt.CryptoObject(signature),
            title = "Per Operation Authentication",
            description = "Sign",
            onAuthenticated = {
                signPerOp(it, activity, { statusText = it })
                showPerOpDialog = false
            },
            onFailedAuthentication = {
                showPerOpDialog = false
                Toast.makeText(
                    activity,
                    "PerOp - Authentication failed $it",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    if (showTimeBasedDialog) {
        showPerOpDialog = false
        statusText = ""
        BiometricPromptDialog(
            context = activity,
            cryptoObject = null,
            title = "Time Based Authentication",
            description = "Sign",
            onAuthenticated = {
                showTimeBasedDialog = false
                signTimeBased(activity, { statusText = it })
            },
            onFailedAuthentication = {
                showTimeBasedDialog = false
                Toast.makeText(
                    activity,
                    "TimeBased - Authentication failed $it",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}

private fun signPerOp(
    result1: BiometricPrompt.AuthenticationResult,
    activity: MainActivity,
    statusText: (exception: String) -> Unit
) {
    try {
        val result = sign(result1.cryptoObject!!.signature!!, "SecretData".toByteArray())
        Toast.makeText(
            activity,
            "PerOp - Signed data: ${result?.size} bytes.",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            activity,
            "PerOp - Signing failed: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        statusText(e.message ?: "Unknown error")
    }
}


private fun signTimeBased(activity: FragmentActivity, statusText: (exception: String) -> Unit) {
    try {
        val result = sign(TIME_BASED_KEY_ALIAS, "SecretData".toByteArray())
        Toast.makeText(
            activity,
            "PerOp - Signed data: ${result?.size} bytes.",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            activity,
            "TimeBased - Signing failed: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        statusText(e.message ?: "Unknown error")
    }
}