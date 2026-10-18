package com.fernandapizzinatto.oralvita.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

private val FigmaBackground = Color(0xFFF0F6FC)
private val FigmaWhite = Color(0xFFFFFFFF)
private val FigmaHeaderGreen = Color(0xFF2EA97D)
private val FigmaHeaderLightGreen = Color(0xFF38B287)
private val FigmaTextBlack = Color(0xFF1E293B)
private val FigmaTextGrey = Color(0xFF64748B)

@Composable
fun SettingsScreen(
    navController: NavController
) {
    val auth = FirebaseAuth.getInstance()

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = FigmaBackground
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Cabeçalho
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        FigmaHeaderGreen,
                        RoundedCornerShape(
                            bottomStart = 32.dp,
                            bottomEnd = 32.dp
                        )
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp
                    )
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                FigmaHeaderLightGreen,
                                RoundedCornerShape(50.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = FigmaWhite
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(14.dp)
                    )

                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = FigmaWhite,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "Configurações",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaWhite
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {

                // Conta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FigmaWhite
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "Conta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaTextBlack
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = auth.currentUser?.email
                                ?: "Usuário",
                            fontSize = 13.sp,
                            color = FigmaTextGrey
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // Sair da conta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FigmaWhite
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text(
                                text = "Sair da conta",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaTextBlack
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = "Encerrar a sessão neste dispositivo.",
                            fontSize = 13.sp,
                            color = FigmaTextGrey
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Button(
                            onClick = {
                                showLogoutDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444)
                            )
                        ) {

                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text(
                                text = "Sair da conta"
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmação de logout
    if (showLogoutDialog) {

        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },

            title = {
                Text(
                    text = "Sair da conta?"
                )
            },

            text = {
                Text(
                    text = "Você precisará fazer login novamente para acessar o OralVita."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        showLogoutDialog = false

                        auth.signOut()

                        navController.navigate("login") {

                            popUpTo(0) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }
                ) {

                    Text(
                        text = "Sair",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {

                    Text(
                        text = "Cancelar",
                        color = FigmaTextGrey
                    )
                }
            }
        )
    }
}