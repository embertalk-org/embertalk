package edu.kit.tm.ps.embertalk.ui.contacts

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import edu.kit.tm.ps.embertalk.Preferences
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.sync.bluetooth.BleSyncService
import edu.kit.tm.ps.embertalk.ui.Screen
import java.util.UUID

@Composable
fun ContactsView(
    navController: NavController,
    contactsViewModel: ContactsViewModel,
    modifier: Modifier = Modifier
) {

    val contactsUiState by contactsViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(9f)
        ) {
            items(contactsUiState.contacts) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable {
                            navController.navigate("contact/${item.userId}")
                        }
                ) {
                    Text(
                        text = item.name,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = {
                navController.navigate(Screen.AddContact.route)
            },
            modifier = Modifier
                .align(Alignment.End)
                .weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.scan_qr_code)
            )
        }
    }
}

@Composable
fun ContactsActions(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    IconButton(onClick = { BleSyncService.startOrPromptBluetooth(context) }) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = stringResource(R.string.start_service)
        )
    }
    IconButton(onClick = {
        if (prefs.getString(Preferences.PUBLIC_KEY, "") == "") {
            Toast.makeText(context, "You need to generate your keys first!", Toast.LENGTH_SHORT).show()
        } else {
            navController.navigate(Screen.qrCodeRoute(UUID.fromString(prefs.getString(Preferences.USER_ID, ""))))
        }
    }) {
        Icon(
            imageVector = Icons.Filled.QrCode,
            contentDescription = stringResource(R.string.start_service)
        )
    }
}