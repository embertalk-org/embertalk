package edu.kit.tm.ps.embertalk.ui.contacts

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.model.contacts.Contact
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactView(
    contactsViewModel: ContactsViewModel,
    navController: NavController,
) {
    val context = LocalContext.current

    val name = rememberSaveable { mutableStateOf ("") }
    val keyParts = remember { mutableStateMapOf(0 to "") }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result == null || !result.contents.startsWith("ember://")) {
                Toast.makeText(context, "Not a valid EmberTalk-Code", Toast.LENGTH_SHORT).show()
            } else {
                val withoutPrefix = result.contents.removePrefix("ember://")
                val parts = withoutPrefix.split("/")
                keyParts[parts[0].toInt()] = parts[1]
            }
        }
    )

    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        OutlinedTextField(
            label = { Text("Contact Name") },
            value = name.value,
            singleLine = true,
            onValueChange = { name.value = it },
            modifier = Modifier.padding(10.dp)
        )
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Scanned Key Parts",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(10.dp)
            )
            LazyRow(
                Modifier.padding(10.dp)
            ) {
                itemsIndexed(keyParts.entries.toList()) { index, item ->
                    if (item.value != "") {
                        Card(
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Text(
                                text = "%s".format(item.key),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
        Row {
            IconButton(
                onClick = {
                    if (name.value != "") {
                        contactsViewModel.viewModelScope.launch {
                            val pubKey = buildString {
                                for (i in 1..keyParts.size) {
                                    append(keyParts[i])
                                }
                            }
                            contactsViewModel.add(Contact(name = name.value, pubKey = pubKey))
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = stringResource(R.string.scan_qr_code)
                )
            }
            IconButton(
                onClick = { scanLauncher.launch(ScanOptions()) },
                modifier = Modifier
                    .padding(10.dp)
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = stringResource(R.string.scan_qr_code)
                )
            }
        }
    }
}