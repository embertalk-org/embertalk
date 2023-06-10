package edu.kit.tm.ps.embertalk.composables

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.sync.MacAddressUtils

@Preview
@Composable
fun SettingsView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)!!
    Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column {
            SubmittableTextField(
                label = { Text(stringResource(R.string.your_mac_address)) },
                imageVector = Icons.Rounded.Build,
                initialValue = prefs.getString("mac", "")!!,
                clearOnSubmit = false,
                inputValidator = { MacAddressUtils.isValidMacAddress(it) },
                onSubmit = { prefs.edit().putString("mac", it.uppercase()).apply() }
            )
            Row {
                InfoDialogButton(
                    alertTitle = { Text(stringResource(R.string.mac_address_how_to)) },
                    alertText = { Text(stringResource(R.string.mac_address_how_to_text)) },
                )
                Text(
                    text = stringResource(R.string.where_to_find_mac),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
        SubmittableTextField(
            label = { Text(stringResource(R.string.your_username)) },
            imageVector = Icons.Rounded.Person,
            initialValue = prefs.getString("currentname", "")!!,
            clearOnSubmit = false,
            onSubmit = { prefs.edit().putString("currentname", it.uppercase()).apply() }
        )
    }
}