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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.storage.DecodedMessage
import edu.kit.tm.ps.embertalk.storage.Message
import edu.kit.tm.ps.embertalk.sync.Synchronizer

@Preview
@Composable
fun MessageView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        val messages = rememberSaveable { mutableStateOf(Synchronizer.store.messages().toList()) }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(9f)
        ) {
            itemsIndexed(messages.value) { _, item ->
                val msgContent = item.bytes.decodeToString()
                MessageCard(message = DecodedMessage("Ember", msgContent))
            }
        }
        SubmittableTextField(
            label = { Text(stringResource(R.string.your_message)) },
            imageVector = Icons.Rounded.Send,
            onSubmit = {
                Synchronizer.store.save(Message(it.encodeToByteArray()))
                messages.value = Synchronizer.store.messages().toList()
            },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}