package edu.kit.tm.ps.embertalk.composables.message_view

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.composables.MessageCard
import edu.kit.tm.ps.embertalk.composables.SubmittableTextField
import edu.kit.tm.ps.embertalk.storage.DecodedMessage
import kotlinx.coroutines.launch

@Composable
fun MessageView(
    messageViewModel: MessageViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)!!

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
                val decoded = DecodedMessage.decode(item)
                val currentName = prefs.getString("currentname", "").toString()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (decoded.from == currentName) {Arrangement.End} else {Arrangement.Start}
                ) {
                    MessageCard(
                        message = decoded
                    )
                }
            }
        }
        SubmittableTextField(
            label = { Text(stringResource(R.string.your_message)) },
            imageVector = Icons.Rounded.Send,
            onSubmit = {
                messageViewModel.viewModelScope.launch {
                    messageViewModel.saveMessage(DecodedMessage(prefs.getString("currentname", "").toString(), it).encode())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}