package com.test.bug

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.test.bug.MainViewModel.DataSize
import com.test.bug.ProjectApp.Companion.PER_OP_KEY_ALIAS
import com.test.bug.ProjectApp.Companion.TIME_BASED_KEY_ALIAS
import com.test.bug.ui.theme.KeystoreDebuggingTheme

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val showPerOpDialog by viewModel.showDialog

            KeystoreDebuggingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    ActionButtons(
                        activity = this,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        viewModel = viewModel
                    )

                    if (showPerOpDialog) {
                        val signature = getSignature(PER_OP_KEY_ALIAS)

                        BiometricPromptDialog(
                            context = this,
                            cryptoObject = BiometricPrompt.CryptoObject(signature),
                            title = "Per Operation Authentication",
                            description = "Sign",
                            onAuthenticated = {
                                viewModel.signPerOp(it)
                            },
                            onFailedAuthentication = {
                                viewModel.onFailed()
                                Toast.makeText(
                                    this,
                                    "PerOp - Authentication failed $it",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtons(modifier: Modifier = Modifier, activity: MainActivity, viewModel: MainViewModel) {
    var statusText by viewModel.statusText
    var showTimeBasedDialog by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(DataSize.DATA_256) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Choose an action")
        Button(onClick = {
            viewModel.onButtonClick(DataSize.DATA_256, PER_OP_KEY_ALIAS)
        }) {
            Text("Per-Op auth (256 bytes)")
        }
        Button(onClick = {
            viewModel.onButtonClick(DataSize.DATA_257, PER_OP_KEY_ALIAS)
        }) {
            Text("Per-Op auth (257 bytes)")
        }
        Button(onClick = {
            showTimeBasedDialog = true
            size = DataSize.DATA_256
        }) {
            Text(text = "Time Based auth (256 bytes)")
        }
        Button(onClick = {
            showTimeBasedDialog = true
            size = DataSize.DATA_257
        }) {
            Text(text = "Time Based auth (257 bytes)")
        }
        Text(statusText)
    }



    if (showTimeBasedDialog) {
        statusText = ""
        BiometricPromptDialog(
            context = activity,
            title = "Time Based Authentication",
            description = "Sign",
            onAuthenticated = {
                showTimeBasedDialog = false
                viewModel.signTimeBased(size)
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