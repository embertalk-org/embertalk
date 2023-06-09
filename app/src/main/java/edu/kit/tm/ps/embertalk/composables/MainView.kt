package edu.kit.tm.ps.embertalk.composables

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.storage.Message
import edu.kit.tm.ps.embertalk.sync.MacAddressUtils
import edu.kit.tm.ps.embertalk.sync.Synchronizer
import edu.kit.tm.ps.embertalk.sync.bluetooth.BluetoothSyncService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)!!
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
                navController.navigate("messages")
            }) {
                Text(stringResource(R.string.show_messages))
            }
        }
    }
}