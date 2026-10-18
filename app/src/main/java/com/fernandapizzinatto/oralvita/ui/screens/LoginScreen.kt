package com.fernandapizzinatto.oralvita.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fernandapizzinatto.oralvita.R // Certifique-se de que o pacote do seu R está correto
import com.fernandapizzinatto.oralvita.ui.model.GameState
import com.fernandapizzinatto.oralvita.ui.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Cores do Tema Figma - Login
private val FigmaBackground = Color(0xFFF0F6FC)
private val FigmaWhite = Color(0xFFFFFFFF)
private val FigmaPrimaryBlue = Color(0xFF4A82C5)
private val FigmaDarkBlue = Color(0xFF1E293B)
private val FigmaTextGrey = Color(0xFF64748B)
private val FigmaLightBorder = Color(0xFFE2E8F0)

@Composable
fun LoginScreen(
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        containerColor = FigmaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(FigmaBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Seção Superior: Logo + Título
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card do Logo com a Imagem Real
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), clip = false)
                        .clip(RoundedCornerShape(24.dp))
                        .background(FigmaWhite)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.oralvita),
                        contentDescription = "Logo OralVita",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "OralVita",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaDarkBlue
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Monitoramento da Saúde Bucal",
                    fontSize = 14.sp,
                    color = FigmaTextGrey,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Formulário de Login
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input E-mail
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "E-mail",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FigmaDarkBlue,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("seu@email.com", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = FigmaPrimaryBlue
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FigmaWhite,
                            unfocusedContainerColor = FigmaWhite,
                            focusedBorderColor = FigmaPrimaryBlue,
                            unfocusedBorderColor = FigmaLightBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Senha
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Senha",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FigmaDarkBlue,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = senha,
                        onValueChange = { senha = it },
                        placeholder = { Text("••••••••", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = FigmaPrimaryBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = "Alternar visibilidade",
                                    tint = FigmaTextGrey
                                )
                            }
                        },
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FigmaWhite,
                            unfocusedContainerColor = FigmaWhite,
                            focusedBorderColor = FigmaPrimaryBlue,
                            unfocusedBorderColor = FigmaLightBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Esqueci minha senha
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(
                        onClick = {
                            Toast.makeText(navController.context, "Recuperação de senha solicitada.", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Esqueci minha senha",
                            fontSize = 12.sp,
                            color = FigmaPrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão Entrar
                Button(
                    onClick = {
                        if (email.isBlank() || senha.isBlank()) {
                            Toast.makeText(navController.context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        auth.signInWithEmailAndPassword(email, senha)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser = auth.currentUser
                                    if (firebaseUser != null) {
                                        db.collection("usuarios")
                                            .document(firebaseUser.uid)
                                            .get()
                                            .addOnSuccessListener { document ->
                                                val user = document.toObject(User::class.java)
                                                if (user != null) {
                                                    GameState.points = user.pontos
                                                    GameState.completedMissions = user.missoesConcluidas
                                                    GameState.streak = user.diasConsecutivos
                                                }
                                                Toast.makeText(
                                                    navController.context,
                                                    "Login realizado com sucesso!",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                navController.navigate("home") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        navController.context,
                                        "E-mail ou senha inválidos.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaPrimaryBlue)
                ) {
                    Text(
                        text = "Entrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaWhite
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divisor "ou"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = FigmaLightBorder)
                    Text(
                        text = "ou",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontSize = 12.sp,
                        color = FigmaTextGrey
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = FigmaLightBorder)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Ainda não possui uma conta?",
                    fontSize = 13.sp,
                    color = FigmaTextGrey,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Botão Criar Conta (Outlined)
                OutlinedButton(
                    onClick = {
                        navController.navigate("register")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = FigmaWhite),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, FigmaPrimaryBlue)
                ) {
                    Text(
                        text = "Criar conta",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaPrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rodapé
            Text(
                text = "Desenvolvido para auxiliar o acompanhamento da saúde bucal.",
                fontSize = 11.sp,
                color = FigmaTextGrey.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}