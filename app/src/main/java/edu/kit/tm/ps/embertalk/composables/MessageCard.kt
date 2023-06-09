package edu.kit.tm.ps.embertalk.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = message.from,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = message.content)
        }
    }
}

@Preview
@Composable
private fun MessageCardPreview() {
    MessageCard(message = DecodedMessage("Peter", "Hello there my friend!"))
}