package edu.kit.tm.ps.embertalk.ui.settings

import android.webkit.URLUtil
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.crypto.SyncState
import edu.kit.tm.ps.embertalk.ui.components.SubmittableTextField
import kotlinx.coroutines.launch

@Composable
fun SettingsView(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by settingsViewModel.uiState.collectAsState()

    Column(
        modifier = modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column {
            SubmittableTextField(
                label = { Text(stringResource(R.string.sync_interval)) },
                imageVector = Icons.Filled.Save,
                initialValue = uiState.syncInterval.toString(),
                clearOnSubmit = false,
                inputValidator = { it.toLongOrNull() != null },
                onSubmit = { settingsViewModel.updateSyncInterval(it.toLong()) }
            )
            RatchetState(syncState = uiState.syncState)
            RegenerateKeysButton(uiState.syncState, settingsViewModel)
            DeleteAllButton(settingsViewModel)
            SubmittableTextField(
                label = { Text(stringResource(R.string.key_server_url)) },
                imageVector = Icons.Filled.Save,
                initialValue = uiState.keyServerUrl,
                clearOnSubmit = false,
                inputValidator = URLUtil::isValidUrl,
                onSubmit = settingsViewModel::updateKeyServer
            )
        }
    }
}

@Composable
fun RatchetState(
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = stringResource(R.string.synchronization_state),
            style = MaterialTheme.typography.headlineSmall
        )
        Row {
            when (syncState) {
                is SyncState.Synchronized -> Text(
                    text = stringResource(R.string.synced) + " ${syncState.currentEpoch}",
                    color = Color.Green,
                    modifier = modifier.align(Alignment.CenterVertically)
                )
                is SyncState.Synchronizing -> Text(
                    text = stringResource(R.string.remaining_epochs_format).format(syncState.remainingEpochs),
                    modifier = modifier.align(Alignment.CenterVertically)
                )
                is SyncState.Initializing -> Text(
                    text = stringResource(R.string.still_initializing),
                    modifier = modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun RegenerateKeysButton(
    syncState: SyncState,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val openDialog = remember { mutableStateOf(false) }
    TextButton(
        enabled = syncState is SyncState.Synchronized,
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
                        coroutineScope.launch { settingsViewModel.regenerateKeys() }
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
    settingsViewModel: SettingsViewModel,
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
                        settingsViewModel.viewModelScope.launch { settingsViewModel.deleteAll() }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = { openDialog.value = false }
        )
    }
}