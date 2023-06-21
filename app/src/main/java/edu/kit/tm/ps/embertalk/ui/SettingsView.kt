package edu.kit.tm.ps.embertalk.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import edu.kit.tm.ps.embertalk.Preferences
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.crypto.SyncState
import edu.kit.tm.ps.embertalk.sync.MacAddressUtils
import edu.kit.tm.ps.embertalk.ui.message_view.MessageViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsView(
    cryptoService: CryptoService,
    messageViewModel: MessageViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)!!

    val syncState = remember { mutableStateOf(cryptoService.syncState()) }
    Column(
        modifier = modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column {
            SubmittableTextField(
                label = { Text(stringResource(R.string.your_mac_address)) },
                imageVector = Icons.Filled.Save,
                initialValue = prefs.getString(Preferences.MAC_ADDRESS, "")!!,
                clearOnSubmit = false,
                inputValidator = { MacAddressUtils.isValidMacAddress(it) },
                onSubmit = { prefs.edit().putString(Preferences.MAC_ADDRESS, it.uppercase()).apply() }
            )
            Row {
                InfoDialogButton(
                    alertTitle = { Text(stringResource(R.string.mac_address_how_to)) },
                    alertText = { Text(stringResource(R.string.mac_address_how_to_text)) },
                )
                Text(
                    text = stringResource(R.string.where_to_find_mac),
                    modifier = modifier.align(Alignment.CenterVertically)
                )
            }
            RatchetState(syncState, cryptoService)
            RegenerateKeysButton(syncState, cryptoService)
            DeleteAllButton(messageViewModel = messageViewModel)
        }
    }
}

@Composable
fun RatchetState(
    syncState: MutableState<SyncState>,
    cryptoService: CryptoService,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "Synchronization State",
            style = MaterialTheme.typography.headlineSmall
        )
        Row {
            when (val currentState = syncState.value) {
                is SyncState.Synchronized -> Text(
                    text = stringResource(R.string.synced),
                    color = Color.Green,
                    modifier = modifier.align(Alignment.CenterVertically)
                )
                is SyncState.Synchronizing -> Text(
                    text = stringResource(R.string.remaining_epochs_format).format(currentState.remainingEpochs),
                    modifier = modifier.align(Alignment.CenterVertically)
                )
                is SyncState.Initializing -> Text(
                    text = stringResource(R.string.still_initializing),
                    modifier = modifier.align(Alignment.CenterVertically)
                )
            }
            IconButton(
                onClick = { syncState.value = cryptoService.syncState() },
                modifier = modifier.align(Alignment.CenterVertically)
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(id = R.string.refresh))
            }
        }
    }
}

@Composable
fun RegenerateKeysButton(
    syncState: MutableState<SyncState>,
    cryptoService: CryptoService,
    modifier: Modifier = Modifier
) {
    val openDialog = remember { mutableStateOf(false) }
    TextButton(
        enabled = syncState.value == SyncState.Synchronized,
        onClick = { openDialog.value = true }
    ) {
        Row {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = stringResource(R.string.alert),
                modifier = modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = stringResource(R.string.regenerate_keys),
                modifier = modifier
                    .padding(5.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
    if (openDialog.value) {
        AlertDialog(
            title = { Text(stringResource(R.string.regenerate_keys_alert_title)) },
            text = { Text(stringResource(R.string.regenerate_keys_alert_text)) },
            dismissButton = {
                Button(onClick = { openDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                val coroutineScope = rememberCoroutineScope()
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    onClick = {
                        openDialog.value = false
                        coroutineScope.launch { cryptoService.regenerate() }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = { openDialog.value = false }
        )
    }
}

@Composable
fun DeleteAllButton(
    messageViewModel: MessageViewModel,
    modifier: Modifier = Modifier
) {
    val openDialog = remember { mutableStateOf(false) }
    TextButton(onClick = {
        openDialog.value = true
    }) {
        Row {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = stringResource(R.string.alert),
                modifier = modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = stringResource(R.string.delete_all_messages),
                modifier = modifier
                    .padding(5.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
    if (openDialog.value) {
        AlertDialog(
            title = { Text(stringResource(R.string.delete_all_messages)) },
            text = { Text(stringResource(R.string.delete_all_messages_text)) },
            dismissButton = {
                Button(onClick = { openDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    onClick = {
                        openDialog.value = false
                        messageViewModel.viewModelScope.launch { messageViewModel.deleteAll() }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = { openDialog.value = false }
        )
    }
}