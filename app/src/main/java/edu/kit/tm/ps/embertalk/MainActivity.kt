package edu.kit.tm.ps.embertalk

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.kit.tm.ps.embertalk.app.EmberTalkApplication
import edu.kit.tm.ps.embertalk.ui.EmberTalkApp
import edu.kit.tm.ps.embertalk.ui.theme.EmberTalkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequest(this, Manifest.permission.ACCESS_FINE_LOCATION)
        checkAndRequest(this, Manifest.permission.BLUETOOTH)
        checkAndRequest(this, Manifest.permission.BLUETOOTH_ADMIN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAndRequest(this, Manifest.permission.BLUETOOTH_CONNECT)
            checkAndRequest(this, Manifest.permission.BLUETOOTH_SCAN)
            checkAndRequest(this, Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        setContent {
            EmberTalkTheme {
                val app = this.application as EmberTalkApplication
                EmberTalkApp(
                    keys = app.container.keys
                )
            }
        }
    }
}

fun checkAndRequest(activity: Activity, permission: String) {
    if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), 2)
        return
    }
}