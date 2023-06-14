package edu.kit.tm.ps.embertalk.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.kit.tm.ps.embertalk.storage.DecodedMessage

@Composable
fun MessageCard(
    message: DecodedMessage,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = Modifier.padding(5.dp)
    ) {
        Text(
            text = message.content,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Preview
@Composable
private fun MessageCardPreview() {
    MessageCard(message = DecodedMessage("Hello there my friend!", 0))
}