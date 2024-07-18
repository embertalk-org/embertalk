@file:OptIn(ExperimentalMaterial3Api::class)

package edu.kit.tm.ps.embertalk.ui.message_view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.ui.EmberScaffold
import edu.kit.tm.ps.embertalk.ui.components.MessageCard
import edu.kit.tm.ps.embertalk.ui.components.SubmittableTextField
import edu.kit.tm.ps.embertalk.ui.contacts.ContactsViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MessageView(
    contactId: UUID,
    navController: NavController,
    contactsViewModel: ContactsViewModel,
    messageViewModel: MessageViewModel,
    modifier: Modifier = Modifier
) {
    val messageUiState by messageViewModel.uiState.collectAsState()
    val messages = messageUiState.messages
        .filter { it.senderUserId == contactId || it.recipient == contactId }
        .sortedBy { it.timestamp }
    val contact = messageUiState.contacts.find { it.userId == contactId }!!

    EmberScaffold(
        navController = navController,
        title = contact.name,
        toolWindow = true
    ) {
        Column(
            modifier = modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = messages.size)
            val coroutineScope = rememberCoroutineScope()
            LazyColumn(
                state = listState,
                modifier = modifier
                    .fillMaxWidth()
                    .weight(9f)
            ) {
                coroutineScope.launch {
                    listState.animateScrollToItem(messages.size)
                }
                items(messages) { item ->
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        horizontalArrangement = if (item.senderUserId == contactsViewModel.myId()) { Arrangement.End } else { Arrangement.Start }
                    ) {
                        MessageCard(
                            message = item.content,
                            timestamp = item.timestamp
                        )
                    }
                }
            }
            SubmittableTextField(
                label = { stringResource(R.string.your_message) },
                imageVector = Icons.Filled.Send,
                singleLine = false,
                onSubmit = {
                    messageViewModel.viewModelScope.launch {
                        messageViewModel.saveMessage(it, contact.userId, contact.pubKey)
                    }
                },
            )
        }
    }

}