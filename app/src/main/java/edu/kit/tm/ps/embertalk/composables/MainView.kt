package edu.kit.tm.ps.embertalk.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.sync.bluetooth.BluetoothSyncService

@Composable
fun MainView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        Row {
            ElevatedButton(
                onClick = { BluetoothSyncService.startOrPromptBluetooth(context) }
            ) {
                Text(stringResource(R.string.start_service))
            }
        }
    }
}