package edu.kit.tm.ps.embertalk.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.padding(5.dp)
    ) {
        Text(
            text = message,
            modifier = modifier.padding(10.dp)
        )
    }
}

@Preview
@Composable
private fun MessageCardPreview() {
    MessageCard(message = "Hello there my friend!")
}