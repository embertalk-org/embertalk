package edu.kit.tm.ps.embertalk.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date


val dateFormat: DateFormat = SimpleDateFormat.getTimeInstance()

@Composable
fun MessageCard(
    message: String,
    recipient: String?,
    timestamp: Long,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.padding(5.dp)
    ) {
        Column(
            modifier = modifier.padding(10.dp)
        ) {
            Text(
                text = message,
            )
            val date = dateFormat.format(Date.from(Instant.ofEpochMilli(timestamp)))
            Text(
                text = if (recipient != null) { "$date > $recipient" } else { date },
                fontSize = 2.em,
                modifier = modifier.align(Alignment.End)
            )
        }
    }
}

@Preview
@Composable
private fun MessageCardPreview() {
    MessageCard(
        message = "Hello there my friend!",
        timestamp = 10000000,
        recipient = "Test"
    )
}