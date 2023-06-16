@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package edu.kit.tm.ps.embertalk.ui

import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.preference.PreferenceManager
import edu.kit.tm.ps.embertalk.Preferences
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.app.AppViewModelProvider
import edu.kit.tm.ps.embertalk.sync.bluetooth.BluetoothSyncService
import edu.kit.tm.ps.embertalk.ui.contacts.AddContactView
import edu.kit.tm.ps.embertalk.ui.contacts.ContactsView
import edu.kit.tm.ps.embertalk.ui.contacts.ContactsViewModel
import edu.kit.tm.ps.embertalk.ui.message_view.MessageView
import edu.kit.tm.ps.embertalk.ui.message_view.MessageViewModel
import edu.kit.tm.ps.embertalk.ui.qr_code.QrCodeView

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val resourceId: Int) {
    object Contacts : Screen("contacts", Icons.Filled.Contacts, R.string.contacts)
    object AddContact : Screen("contacts/add", Icons.Filled.QrCodeScanner, R.string.scan_qr_code)
    object Messages : Screen("messages", Icons.Filled.Send, R.string.messages)
    object Settings : Screen("settings", Icons.Filled.Settings, R.string.settings)
    object QrCode : Screen("qr/{pubKey}", Icons.Filled.QrCode, R.string.qr_code)

    companion object {
        fun qrCodeRoute(pubKey: String): String {
            return QrCode.route.replace("{pubKey}", pubKey)
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Composable
fun EmberTalkApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Surface {
        val contactsViewModel: ContactsViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val messageViewModel: MessageViewModel = viewModel(factory = AppViewModelProvider.Factory)
        NavHost(
            navController = navController,
            startDestination = Screen.Messages.route,
        ) {
            composable(Screen.Contacts.route) {
                EmberScaffold(navController = navController, title = stringResource(id = R.string.contacts)) {
                    ContactsView(contactsViewModel = contactsViewModel, navController = navController)
                }
            }
            composable(Screen.AddContact.route) {
                EmberScaffold(navController = navController, title = stringResource(R.string.add_contact), toolWindow = true) {
                    AddContactView(contactsViewModel, navController)
                }
            }
            composable(Screen.Messages.route) {
                EmberScaffold(navController = navController, title = stringResource(id = R.string.messages)) {
                    MessageView(contactsViewModel = contactsViewModel, messageViewModel = messageViewModel)
                }
            }
            composable(Screen.Settings.route) {
                EmberScaffold(navController = navController, title = stringResource(id = R.string.settings)) {
                    SettingsView(messageViewModel)
                }
            }
            composable(
                Screen.QrCode.route,
                arguments = listOf(navArgument("pubKey") { type = NavType.StringType })
            ) {
                EmberScaffold(navController = navController, title = stringResource(R.string.share_contact), toolWindow = true) {
                    val key = it.arguments!!.getString("pubKey")!!
                    QrCodeView(key)
                }
            }
        }
    }
}

@Composable
fun EmberScaffold(
    navController: NavController,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.embertalk),
    toolWindow: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    
    val items = listOf(
        Screen.Contacts,
        Screen.Messages,
        Screen.Settings
    )

    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                     if (toolWindow) {
                         IconButton(onClick = { navController.popBackStack() }) {
                             Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                         }
                     }
                },
                actions = {
                    if (!toolWindow) {
                        IconButton(onClick = { BluetoothSyncService.startOrPromptBluetooth(context) }) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = stringResource(R.string.start_service)
                            )
                        }
                        IconButton(onClick = {
                            if (prefs.getString(Preferences.PUBLIC_KEY, "") == "") {
                                Toast.makeText(context, "You need to generate your keys first!", Toast.LENGTH_SHORT).show()
                            } else {
                                navController.navigate(Screen.qrCodeRoute(prefs.getString(Preferences.PUBLIC_KEY, "")!!))
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.QrCode,
                                contentDescription = stringResource(R.string.start_service)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!toolWindow) {
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
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .padding(10.dp)) {
            content.invoke()
        }
    }
}