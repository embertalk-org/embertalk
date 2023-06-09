package edu.kit.tm.ps.embertalk.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.storage.DecodedMessage
import edu.kit.tm.ps.embertalk.storage.Message
import edu.kit.tm.ps.embertalk.sync.Synchronizer

@Composable
fun MessageView(
    messages: List<Message>,
    onMessageSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(9f)
        ) {
            itemsIndexed(messages) { _, item ->
                val msgContent = item.bytes.decodeToString()
                MessageCard(message = DecodedMessage("Ember", msgContent))
            }
        }
        SubmittableTextField(
            label = { Text(stringResource(R.string.your_message)) },
            imageVector = Icons.Rounded.Send,
            onSubmit = {
                Synchronizer.store.save(Message(it.encodeToByteArray()))
                onMessageSend.invoke()
            },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}