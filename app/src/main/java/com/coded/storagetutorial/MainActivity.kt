package com.coded.storagetutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coded.storagetutorial.storage.InternalStorage
import com.coded.storagetutorial.ui.Home
import com.coded.storagetutorial.ui.camera.MainContent
import com.coded.storagetutorial.ui.theme.StorageTutorialTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val storage = InternalStorage(context)
            StorageTutorialTheme {
                NavHost(navController = navController, startDestination = Screen.Home.route) {
                    composable(Screen.Home.route) {
                        Home(navController, storage)
                    }
                    composable(Screen.Camera.route) {
                        MainContent(storage = storage)
                    }
                }
            }
        }

        //What is internal Storage? this is the private storage of your app, no other app can access the inx storage
        //of your app, unless you root your phone, if you want to save sensitive files only for the users then use this
    }
}

sealed class Screen(
    val route: String
) {
    object Home: Screen("home")
    object Camera: Screen("camera")
}