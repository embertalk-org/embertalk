package edu.kit.tm.ps.embertalk.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.kit.tm.ps.embertalk.storage.DecodedMessage
import edu.kit.tm.ps.embertalk.sync.Synchronizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageView(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        TopAppBar(
            title = { Text("Messages") },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
        )
        LazyColumn {
            itemsIndexed(Synchronizer.store.messages().map { it.bytes.decodeToString() }) { index, item ->
                MessageCard(message = DecodedMessage("Ember", item))
            }
        }
    }
}