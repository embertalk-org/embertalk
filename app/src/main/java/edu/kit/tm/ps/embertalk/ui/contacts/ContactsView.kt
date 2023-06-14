package edu.kit.tm.ps.embertalk.ui.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.ui.Screen
import kotlinx.coroutines.launch

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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = item.name,
                            modifier
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            onClick = { navController.navigate(Screen.qrCodeRoute(item.pubKey)) },
                        ) {
                            Icon(imageVector = Icons.Filled.QrCode, contentDescription = "delete")
                        }
                        IconButton(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            onClick = { contactsViewModel.viewModelScope.launch { contactsViewModel.delete(item) } },
                        ) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
                        }
                    }
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