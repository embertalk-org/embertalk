package edu.kit.tm.ps.embertalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import edu.kit.tm.ps.embertalk.ui.EmberTalkApp
import edu.kit.tm.ps.embertalk.ui.theme.EmberTalkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmberTalkTheme {
                EmberTalkApp()
            }
        }
    }
}