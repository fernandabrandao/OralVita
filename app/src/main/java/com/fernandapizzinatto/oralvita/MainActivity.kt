package com.fernandapizzinatto.oralvita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fernandapizzinatto.oralvita.navigation.AppNavigation
import com.fernandapizzinatto.oralvita.ui.theme.OralVitaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            OralVitaTheme {
                AppNavigation()
            }
        }
    }
}