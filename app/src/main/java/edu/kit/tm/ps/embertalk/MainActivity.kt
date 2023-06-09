@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package edu.kit.tm.ps.embertalk

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.kit.tm.ps.embertalk.composables.SubmittableTextField
import edu.kit.tm.ps.embertalk.storage.Message
import edu.kit.tm.ps.embertalk.sync.MacAddressUtils
import edu.kit.tm.ps.embertalk.sync.Synchronizer
import edu.kit.tm.ps.embertalk.sync.bluetooth.BluetoothSyncService
import edu.kit.tm.ps.embertalk.ui.theme.EmberTalkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequest(this, Manifest.permission.ACCESS_FINE_LOCATION)
        checkAndRequest(this, Manifest.permission.BLUETOOTH)
        checkAndRequest(this, Manifest.permission.BLUETOOTH_ADMIN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAndRequest(this, Manifest.permission.BLUETOOTH_CONNECT)
            checkAndRequest(this, Manifest.permission.BLUETOOTH_SCAN)
            checkAndRequest(this, Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        setContent {
            EmberTalkTheme {
                EmberTalk(
                )
            }
        }
    }
}

fun checkAndRequest(activity: Activity, permission: String) {
    if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), 2)
        return
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Composable
fun EmberTalk(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)!!
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            TopAppBar(
                title = { Text("EmberTalk") }
            )
            SubmittableTextField(
                label = { Text(stringResource(R.string.your_mac_address)) },
                imageVector = Icons.Rounded.Build,
                initialValue = prefs.getString("mac", "")!!,
                clearOnSubmit = false,
                inputValidator = { MacAddressUtils.isValidMacAddress(it) },
                onSubmit = { prefs.edit().putString("mac", it.uppercase()).apply() }
            )
            SubmittableTextField(
                label = { Text(stringResource(R.string.your_message)) },
                imageVector = Icons.Rounded.Send,
                onSubmit = { Synchronizer.store.save(Message(it.encodeToByteArray())) }
            )
            Row {
                TextButton(
                    onClick = { BluetoothSyncService.startOrPromptBluetooth(context) }
                ) {
                    Text(stringResource(R.string.start_service))
                }
                TextButton(onClick = {
                    val decodedMessages = Synchronizer.store.messages().map { it.bytes.decodeToString() }
                    Toast.makeText(context, decodedMessages.joinToString("\n"), Toast.LENGTH_SHORT).show()
                }) {
                    Text(stringResource(R.string.show_messages))
                }
            }
        }
    }
}