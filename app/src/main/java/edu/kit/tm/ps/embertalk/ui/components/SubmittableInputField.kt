package edu.kit.tm.ps.embertalk.ui.components

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
    inputValidator: (String) -> Boolean = { true },
    initialValue: String = "",
    clearOnSubmit: Boolean = true,
    contentDescription: String = "",
    modifier: Modifier = Modifier
) {
    val msgInput = rememberSaveable { mutableStateOf(initialValue) }

    val onlyWhitespace: () -> Boolean = { msgInput.value.trim() == "" }
    val validInput: () -> Boolean = { !onlyWhitespace() && inputValidator.invoke(msgInput.value) }

    val isError = rememberSaveable { mutableStateOf( !validInput() ) }
    Row {
        OutlinedTextField(
            value = msgInput.value,
            label = label,
            singleLine = true,
            isError = isError.value,
            onValueChange = {
                msgInput.value = it
                isError.value = !validInput()
            }
        )
        IconButton(
            onClick = {
                if (validInput()) {
                    onSubmit.invoke(msgInput.value)
                    if (clearOnSubmit) {
                        msgInput.value = ""
                    }
                    isError.value = !validInput()
                }
            },
            enabled = !isError.value,
            modifier = modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        }
    }
}