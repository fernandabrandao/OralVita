package com.fernandapizzinatto.oralvita.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Cores do Tema Figma - Cadastro
private val FigmaHeaderBlue = Color(0xFF3B82F6)
private val FigmaBackground = Color(0xFFF8FAFC)
private val FigmaWhite = Color(0xFFFFFFFF)
private val FigmaDarkBlue = Color(0xFF1E293B)
private val FigmaTextGrey = Color(0xFF64748B)
private val FigmaLightBorder = Color(0xFFE2E8F0)
private val FigmaGreenButton = Color(0xFF10B981)
private val FigmaGreenCardBg = Color(0xFFECFDF5)
private val FigmaGreenCardBorder = Color(0xFFA7F3D0)
private val FigmaGreenCardText = Color(0xFF065F46)

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String) -> Unit,
    onBackToLoginClick: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }

    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }

    val formularioValido =
        nome.isNotBlank() &&
                email.isNotBlank() &&
                senha.length >= 6 &&
                confirmarSenha == senha

    Scaffold(
        containerColor = FigmaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Curvado Superior Azul
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(FigmaHeaderBlue)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    // Botão Voltar (Seta Redonda)
                    IconButton(
                        onClick = onBackToLoginClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Criar Conta",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Preencha os dados abaixo para começar\na cuidar do seu sorriso.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                }
            }

            // Formulário de Cadastro
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Input Nome
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Nome",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FigmaDarkBlue,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        placeholder = { Text("Seu nome", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Person, contentDescription = null, tint = FigmaHeaderBlue)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FigmaWhite,
                            unfocusedContainerColor = FigmaWhite,
                            focusedBorderColor = FigmaHeaderBlue,
                            unfocusedBorderColor = FigmaLightBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                            Icon(imageVector = Icons.Outlined.Email, contentDescription = null, tint = FigmaHeaderBlue)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FigmaWhite,
                            unfocusedContainerColor = FigmaWhite,
                            focusedBorderColor = FigmaHeaderBlue,
                            unfocusedBorderColor = FigmaLightBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                            Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = FigmaHeaderBlue)
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
                            focusedBorderColor = FigmaHeaderBlue,
                            unfocusedBorderColor = FigmaLightBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Confirmar Senha
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Confirmar senha",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FigmaDarkBlue,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = confirmarSenha,
                        onValueChange = { confirmarSenha = it },
                        placeholder = { Text("••••••••", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = FigmaHeaderBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmarSenhaVisivel = !confirmarSenhaVisivel }) {
                                Icon(
                                    imageVector = if (confirmarSenhaVisivel) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = "Alternar visibilidade",
                                    tint = FigmaTextGrey
                                )
                            }
                        },
                        visualTransformation = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FigmaWhite,
                            unfocusedContainerColor = FigmaWhite,
                            focusedBorderColor = FigmaHeaderBlue,
                            unfocusedBorderColor = FigmaLightBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Erro de senhas não coincidentes
                if (confirmarSenha.isNotEmpty() && confirmarSenha != senha) {
                    Text(
                        text = "As senhas não coincidem.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card Verde de Aviso de Segurança
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FigmaGreenCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FigmaGreenCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = FigmaGreenButton,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Escolha uma senha segura com pelo menos 8 caracteres para proteger sua conta!",
                            fontSize = 12.sp,
                            color = FigmaGreenCardText,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botão Criar Conta (Verde)
                Button(
                    onClick = {
                        onRegisterClick(nome, email, senha)
                    },
                    enabled = formularioValido,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FigmaGreenButton,
                        disabledContainerColor = FigmaGreenButton.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "Criar Conta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Rodapé "Já possui uma conta? Entrar"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Já possui uma conta? ",
                        fontSize = 14.sp,
                        color = FigmaTextGrey
                    )
                    Text(
                        text = "Entrar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaHeaderBlue,
                        modifier = Modifier.clickable { onBackToLoginClick() }
                    )
                }
            }
        }
    }
}