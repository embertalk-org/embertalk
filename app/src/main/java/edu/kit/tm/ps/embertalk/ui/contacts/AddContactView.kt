package edu.kit.tm.ps.embertalk.ui.contacts

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.model.contacts.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactView(
    contactsViewModel: ContactsViewModel,
    navController: NavController,
) {
    val context = LocalContext.current

    val name = rememberSaveable { mutableStateOf("") }
    val id = rememberSaveable { mutableStateOf ("") }
    val keyParts = remember { mutableStateMapOf(0 to "") }
    val key = remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result == null || result.contents == null || !result.contents.startsWith("ember://")) {
                Toast.makeText(context, "Not a valid EmberTalk-Code", Toast.LENGTH_SHORT).show()
            } else {
                val withoutPrefix = result.contents.removePrefix("ember://")
                val parts = withoutPrefix.split("/")
                if (parts[0] == "id") {
                    id.value = parts[1].trim()

                } else {
                    keyParts[parts[0].toInt()] = parts[1].trim()
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom)
    ) {
        OutlinedTextField(
            label = { Text("Name") },
            value = name.value,
            onValueChange = { name.value = it }
        )
        val focusManager = LocalFocusManager.current
        OutlinedTextField(
            label = { Text("ID")},
            readOnly = true,
            value = id.value,
            onValueChange = {}
        )
        IconButton(
            enabled = id.value != "",
            onClick = {
                contactsViewModel.viewModelScope.launch(Dispatchers.IO) {
                    val result = contactsViewModel.downloadKey(id.value)
                    if (result == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to download this key", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        key.value = result
                        focusManager.clearFocus()
                    }
                }
            }
        ) {
            Icon(imageVector = Icons.Filled.Download, contentDescription = "Download")
        }
        Text(
            text = "Scanned Key Parts",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(10.dp, 0.dp)
        )
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            val maxWidth = maxWidth
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedCard(
                    modifier = Modifier.width(maxWidth * 0.8f)
                ) {
                    LazyRow {
                        items(keyParts.entries.toList()) { item ->
                            if (item.value != "") {
                                Card(
                                    modifier = Modifier.padding(1.dp)
                                ) {
                                    Text(
                                        text = "%s".format(item.key),
                                        modifier = Modifier.padding(5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(
                    modifier = Modifier.width(maxWidth * 0.2f),
                    onClick = {
                        key.value = buildString {
                            for (i in 0 until keyParts.size) {
                                append(keyParts[i]!!)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.confirm)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        BoxWithConstraints(
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            val maxWidth = maxWidth
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    label = { Text(stringResource(id = R.string.public_key)) },
                    value = key.value,
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.width(maxWidth * 0.8f),
                    onValueChange = { key.value = it }
                )
                IconButton(
                    modifier = Modifier.width(maxWidth * 0.2f),
                    onClick = {
                        val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipContent = clipBoard.primaryClip
                        if (clipContent != null && clipContent.itemCount > 0) {
                            val content = clipContent.getItemAt(0)
                            if (content.text.startsWith("embertalk://")) {
                                key.value = content.text.toString().removePrefix("embertalk://")
                                return@IconButton
                            }
                        }
                        Toast.makeText(context, R.string.not_an_embertalk_code, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Filled.ContentPaste, contentDescription = "Paste")
                }
            }
        }
        Spacer(modifier = Modifier.weight(9f))
        FloatingActionButton(
            onClick = {
                scanLauncher.launch(ScanOptions())
            },
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = stringResource(R.string.scan_qr_code)
            )
        }
        FloatingActionButton(
            onClick = {
                if (name.value != "" && id.value != "" && key.value != "") {
                    contactsViewModel.viewModelScope.launch {
                        contactsViewModel.add(Contact(name = name.value, userId = UUID.fromString(id.value), pubKey = key.value))
                    }
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = stringResource(R.string.scan_qr_code)
            )
        }
    }
}