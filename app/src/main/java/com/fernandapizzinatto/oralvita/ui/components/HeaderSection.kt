package com.fernandapizzinatto.oralvita.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HeaderSection() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF5DA9F6),
                        Color(0xFF3B82D6)
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(24.dp)
    ) {

        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Boa noite, Fernanda! 👋",
                    color = Color.White,
                    fontSize = 24.sp
                )

                Row {

                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Áudio",
                            tint = Color.White
                        )
                    }

                    Button(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null
                        )

                        Text(" Progresso")
                    }

                }

            }

            Text(
                text = "Vamos cuidar do seu sorriso hoje?",
                color = Color.White,
                fontSize = 28.sp
            )

        }

    }

}