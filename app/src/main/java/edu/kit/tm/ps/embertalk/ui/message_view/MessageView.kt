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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.ui.EmberScaffold
import edu.kit.tm.ps.embertalk.ui.components.MessageCard
import edu.kit.tm.ps.embertalk.ui.components.SubmittableTextField
import kotlinx.coroutines.launch

@Composable
fun MessageView(
    navController: NavController,
    messageViewModel: MessageViewModel,
    modifier: Modifier = Modifier
) {
    val messageUiState by messageViewModel.uiState.collectAsState()
    val messages = messageUiState.messages

    EmberScaffold(
        navController = navController,
        title = messageUiState.contact.name,
        toolWindow = true,
        actions = {
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                onClick = { navController.navigate(messageViewModel.shareContactRoute()) },
            ) {
                Icon(imageVector = Icons.Filled.QrCode, contentDescription = stringResource(R.string.qr_code))
            }
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                onClick = {
                    messageViewModel.viewModelScope.launch { messageViewModel.deleteContact() }
                    navController.popBackStack()
                },
            ) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete))
            }
        }
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
                        horizontalArrangement = if (messageViewModel.isMe(item.senderUserId)) { Arrangement.End } else { Arrangement.Start }
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
                imageVector = Icons.AutoMirrored.Default.Send,
                singleLine = false,
                onSubmit = {
                    messageViewModel.viewModelScope.launch {
                        messageViewModel.saveMessage(it)
                    }
                },
            )
        }
    }

}