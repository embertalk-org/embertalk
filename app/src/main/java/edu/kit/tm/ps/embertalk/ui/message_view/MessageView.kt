package edu.kit.tm.ps.embertalk.ui.message_view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import edu.kit.tm.ps.embertalk.ui.MessageCard
import edu.kit.tm.ps.embertalk.ui.SubmittableTextField
import kotlinx.coroutines.launch

@Composable
fun MessageView(
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
            itemsIndexed(messageUiState.messages) { _, item ->
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
        SubmittableTextField(
            label = { Text(stringResource(R.string.your_message)) },
            imageVector = Icons.Rounded.Send,
            onSubmit = {
                messageViewModel.viewModelScope.launch {
                    messageViewModel.saveMessage(Message(content = it, mine = true, epoch = 0))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}