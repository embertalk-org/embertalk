package edu.kit.tm.ps.embertalk.ui.contacts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import edu.kit.tm.ps.embertalk.model.contacts.Contact

@Composable
fun ContactsView(
    contactsViewModel: ContactsViewModel,
    modifier: Modifier = Modifier
) {

    val contactsUiState by contactsViewModel.uiState.collectAsState()
    ContactsList(contacts = contactsUiState.contacts)
}

@Composable
fun ContactsList(
    contacts: List<Contact>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            itemsIndexed(contacts) { _, item ->
                Card {
                    Text(text = item.name)
                }
            }
        }
    }
}