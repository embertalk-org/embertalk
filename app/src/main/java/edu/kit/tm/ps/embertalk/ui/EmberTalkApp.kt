@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package edu.kit.tm.ps.embertalk.ui

import android.Manifest
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.res.stringResource
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import edu.kit.tm.ps.embertalk.R
import edu.kit.tm.ps.embertalk.app.AppViewModelProvider
import edu.kit.tm.ps.embertalk.ui.components.PermissionsRequired
import edu.kit.tm.ps.embertalk.ui.contacts.AddContactView
import edu.kit.tm.ps.embertalk.ui.contacts.ContactsActions
import edu.kit.tm.ps.embertalk.ui.contacts.ContactsView
import edu.kit.tm.ps.embertalk.ui.contacts.ContactsViewModel
import edu.kit.tm.ps.embertalk.ui.message_view.MessageView
import edu.kit.tm.ps.embertalk.ui.message_view.MessageViewModel
import edu.kit.tm.ps.embertalk.ui.qr_code.QrCodeView
import edu.kit.tm.ps.embertalk.ui.qr_code.QrCodeViewModel
import edu.kit.tm.ps.embertalk.ui.settings.SettingsView
import edu.kit.tm.ps.embertalk.ui.settings.SettingsViewModel
import java.util.UUID

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val resourceId: Int) {
    data object Contacts : Screen("contacts", Icons.Filled.Contacts, R.string.contacts)
    data object AddContact : Screen("contacts/add", Icons.Filled.QrCodeScanner, R.string.scan_qr_code)
    data object Settings : Screen("settings", Icons.Filled.Settings, R.string.settings)
    data object QrCode : Screen("qr/{userId}", Icons.Filled.QrCode, R.string.qr_code)

    companion object {
        fun qrCodeRoute(userId: UUID): String {
            return QrCode.route.replace("{userId}", userId.toString())
        }
    }
}

val NAVIGATION_ITEMS = listOf(
    Screen.Contacts,
    Screen.Settings
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EmberTalkApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Surface(
        modifier = modifier
    ) {
        val contactsViewModel: ContactsViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)

        val permissionState = rememberMultiplePermissionsState(mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                this.add(Manifest.permission.BLUETOOTH_CONNECT)
                this.add(Manifest.permission.BLUETOOTH_SCAN)
                this.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
        })
        PermissionsRequired(
            multiplePermissionsState = permissionState,
            permissionsNotGrantedContent = {
               EmberScaffold(navController = navController, toolWindow = true, showBack = false) {
                   Column(
                       verticalArrangement = Arrangement.spacedBy(10.dp)
                   ) {
                       Text(stringResource(R.string.permission_rationale))
                       ElevatedButton(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                           Text(stringResource(R.string.request_permissions))
                       }
                   }

               }
            },
            permissionsNotAvailableContent = { Text(stringResource(R.string.request_permissions)) }
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Contacts.route,
            ) {
                composable(Screen.Contacts.route) {
                    EmberScaffold(
                        navController = navController,
                        title = stringResource(id = R.string.contacts),
                        bottomBar = {
                            MainBottomBar(navController)
                        },
                        actions = {
                            ContactsActions(navController)
                        }
                    ) {
                        ContactsView(
                            contactsViewModel = contactsViewModel,
                            navController = navController
                        )
                    }
                }
                composable(Screen.AddContact.route) {
                    EmberScaffold(
                        navController = navController,
                        title = stringResource(R.string.add_contact),
                        toolWindow = true
                    ) {
                        AddContactView(contactsViewModel, navController)
                    }
                }
                composable(
                    route = "contact/{contactId}",
                    arguments = listOf(navArgument("contactId") { type = NavType.StringType},)
                ) { backStackEntry ->
                    val contactId = UUID.fromString(backStackEntry.arguments?.getString("contactId"))
                    val messageViewModel: MessageViewModel = viewModel(factory = AppViewModelProvider.messageViewModelFactory(contactId))
                    MessageView(
                        navController = navController,
                        messageViewModel = messageViewModel
                    )
                }
                composable(Screen.Settings.route) {
                    EmberScaffold(
                        navController = navController,
                        title = stringResource(id = R.string.settings),
                        bottomBar = {
                            MainBottomBar(navController)
                        },
                    ) {
                        SettingsView(settingsViewModel)
                    }
                }
                composable(
                    Screen.QrCode.route,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) {
                    EmberScaffold(
                        navController = navController,
                        title = stringResource(R.string.share_contact),
                        toolWindow = true
                    ) {
                        val userId = UUID.fromString(it.arguments!!.getString("userId")!!)
                        val qrCodeViewModel: QrCodeViewModel = viewModel(factory = AppViewModelProvider.qrCodeViewModelFactory(userId))
                        QrCodeView(qrCodeViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmberScaffold(
    navController: NavController,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.embertalk),
    toolWindow: Boolean = false,
    showBack: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (toolWindow && showBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = actions
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        Box(modifier = modifier
            .padding(innerPadding)
            .padding(10.dp)) {
            content.invoke()
        }
    }
}

@Composable
fun MainBottomBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomAppBar {
        NAVIGATION_ITEMS.forEach { screen ->
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