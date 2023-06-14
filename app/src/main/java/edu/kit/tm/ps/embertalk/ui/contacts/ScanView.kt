package edu.kit.tm.ps.embertalk.ui.contacts

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
fun ScanView(
    contactsViewModel: ContactsViewModel,
    navController: NavController,
) {
    val context = LocalContext.current

    val name = rememberSaveable { mutableStateOf ("") }
    val pubKey = rememberSaveable { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result == null || !result.contents.startsWith("embertalk://")) {
                Toast.makeText(context, "Not a valid EmberTalk-Code", Toast.LENGTH_SHORT).show()
            } else {
                pubKey.value = result.contents.removePrefix("embertalk://")
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
        Row {
            OutlinedTextField(
                label = { Text("Public Key (Scan to fill)") },
                value = pubKey.value,
                readOnly = true,
                singleLine = true,
                onValueChange = { pubKey.value = it },
                modifier = Modifier
                    .padding(10.dp)
                    .weight(3f)
            )
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
        IconButton(
            onClick = {
                if (name.value != "" && pubKey.value != "") {
                    contactsViewModel.viewModelScope.launch {
                        contactsViewModel.add(Contact(name = name.value, pubKey = pubKey.value))
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
    }
}