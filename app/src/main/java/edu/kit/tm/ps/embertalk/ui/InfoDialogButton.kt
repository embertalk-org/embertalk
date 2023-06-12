package edu.kit.tm.ps.embertalk.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun InfoDialogButton(
    alertTitle: @Composable () -> Unit,
    alertText: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val openDialog = remember { mutableStateOf(false) }
    IconButton(onClick = {
        openDialog.value = true
    }) {
        Icon(imageVector = Icons.Rounded.Info, contentDescription = "info")
    }
    if (openDialog.value) {
        AlertDialog(
            title = alertTitle,
            text = alertText,
            confirmButton = {
                Button(onClick = { openDialog.value = false }) {
                    Text("Understood")
                }
            },
            onDismissRequest = { openDialog.value = false }
        )
    }
}