package edu.kit.tm.ps.embertalk.composables

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.sync.bluetooth.BluetoothSyncService

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val resourceId: Int) {
    object Messages : Screen("messages", Icons.Filled.Send, R.string.messages)
    object Settings : Screen("settings", Icons.Filled.Settings, R.string.settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Composable
fun EmberTalkApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Messages,
        Screen.Settings
    )
    Surface {
        val context = LocalContext.current

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val messageViewModel: MessageViewModel = viewModel()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("EmberTalk") },
                    actions = {
                        IconButton(onClick = { BluetoothSyncService.startOrPromptBluetooth(context) }) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = stringResource(R.string.start_service)
                            )
                        }
                        IconButton(onClick = { messageViewModel.updateMessages() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(R.string.refresh)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.route) },
                            label = { Text(stringResource(screen.resourceId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Messages.route,
                    modifier = Modifier.padding(10.dp)
                ) {
                    composable(Screen.Messages.route) { MessageView(messageViewModel = messageViewModel) }
                    composable(Screen.Settings.route) { SettingsView() }
                }
            }
        }
    }
}