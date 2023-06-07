package edu.kit.tm.ps.embertalk.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmittableTextField(
    label: @Composable () -> Unit,
    imageVector: ImageVector,
    onSubmit: (String) -> Unit,
    initialValue: String = "",
    clearOnSubmit: Boolean = true,
    contentDescription: String = "",
    modifier: Modifier = Modifier
) {
    val msgInput = rememberSaveable { mutableStateOf(initialValue) }
    Row {
        OutlinedTextField(
            value = msgInput.value,
            label = label,
            singleLine = true,
            onValueChange = { msgInput.value = it }
        )
        IconButton(
            onClick = {
                onSubmit.invoke(msgInput.value)
                if (clearOnSubmit) {
                    msgInput.value = ""
                }
            },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        }
    }
}