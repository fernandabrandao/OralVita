package com.fernandapizzinatto.oralvita.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fernandapizzinatto.oralvita.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: () -> Unit
) {

    LaunchedEffect(Unit) {
        delay(1500)
        onFinish()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_oralvita),
            contentDescription = "Logo OralVita",
            modifier = Modifier.size(220.dp)
        )

        CircularProgressIndicator(
            modifier = Modifier.size(32.dp)
        )
    }
}