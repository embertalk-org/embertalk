@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package edu.kit.tm.ps.embertalk.ui.message_view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import edu.kit.tm.ps.embertalk.ui.MessageCard
import edu.kit.tm.ps.embertalk.ui.contacts.ContactsViewModel
import kotlinx.coroutines.launch

@Composable
fun MessageView(
    cryptoService: CryptoService,
    contactsViewModel: ContactsViewModel,
    messageViewModel: MessageViewModel,
    modifier: Modifier = Modifier
) {

    val messageUiState by messageViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(9f)
        ) {
            items(messageUiState.messages) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (item.mine) { Arrangement.End } else { Arrangement.Start }
                ) {
                    MessageCard(
                        message = item.content
                    )
                }
            }
        }
        SendMessageField(cryptoService = cryptoService, contactsViewModel = contactsViewModel, messageViewModel = messageViewModel)
    }
}

@Composable
fun SendMessageField(
    cryptoService: CryptoService,
    contactsViewModel: ContactsViewModel,
    messageViewModel: MessageViewModel,
    modifier: Modifier = Modifier,
) {
    val message = rememberSaveable { mutableStateOf("") }
    Row {
        OutlinedTextField(
            label = { Text(stringResource(id = R.string.your_message)) },
            value = message.value,
            onValueChange = { message.value = it }
        )
        Spacer(modifier = Modifier.weight(1f))
        SubmitButton(
            cryptoService = cryptoService,
            message = message,
            contactsViewModel = contactsViewModel,
            messageViewModel = messageViewModel
        )
    }
}

@Composable
fun SubmitButton(
    cryptoService: CryptoService,
    message: MutableState<String>,
    contactsViewModel: ContactsViewModel,
    messageViewModel: MessageViewModel,
    modifier: Modifier = Modifier
) {
    val openDialog = remember { mutableStateOf(false) }
    IconButton(
        enabled = message.value != "",
        onClick = {
            openDialog.value = true
        }
    ) {
        Row {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = stringResource(R.string.send_message),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
    if (openDialog.value) {
        Dialog(
            onDismissRequest = { openDialog.value = false }
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = "Send to...",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    LazyColumn {
                        items(contactsViewModel.uiState.value.contacts) { item ->
                            ElevatedCard {
                                Row {
                                    Text(
                                        text = item.name,
                                        modifier = Modifier.padding(10.dp).align(Alignment.CenterVertically)
                                    )
                                    IconButton(
                                        onClick = {
                                            openDialog.value = false
                                            messageViewModel.viewModelScope.launch { messageViewModel.saveMessage(Message(content = message.value, mine = true, timestamp = System.currentTimeMillis()), item.pubKey) }
                                            message.value = ""
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Send,
                                            contentDescription = stringResource(R.string.send_message),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}