package edu.kit.tm.ps.embertalk.ui.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.ui.Screen

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
            itemsIndexed(contactsUiState.contacts) { _, item ->
                Card {
                    Text(text = item.name)
                }
            }
        }
        FloatingActionButton(
            onClick = {
                navController.navigate(Screen.Scan.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = stringResource(R.string.scan_qr_code)
            )
        }
    }
}