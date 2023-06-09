package edu.kit.tm.ps.embertalk.composables

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.sync.MacAddressUtils

@Composable
fun SettingsView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)!!
    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        SubmittableTextField(
            label = { Text(stringResource(R.string.your_mac_address)) },
            imageVector = Icons.Rounded.Build,
            initialValue = prefs.getString("mac", "")!!,
            clearOnSubmit = false,
            inputValidator = { MacAddressUtils.isValidMacAddress(it) },
            onSubmit = { prefs.edit().putString("mac", it.uppercase()).apply() }
        )
    }
}