package com.fernandapizzinatto.oralvita.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fernandapizzinatto.oralvita.ui.model.User
import com.fernandapizzinatto.oralvita.ui.screens.ChallengesScreen
import com.fernandapizzinatto.oralvita.ui.screens.HomeScreen
import com.fernandapizzinatto.oralvita.ui.screens.LoginScreen
import com.fernandapizzinatto.oralvita.ui.screens.ProgressScreen
import com.fernandapizzinatto.oralvita.ui.screens.RegisterScreen
import com.fernandapizzinatto.oralvita.ui.screens.SettingsScreen
import com.fernandapizzinatto.oralvita.ui.screens.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen {
                if (auth.currentUser != null) {
                    navController.navigate("home") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                } else {
                    navController.navigate("login") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                }
            }
        }

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(
                onRegisterClick = { nome, email, senha ->
                    auth.createUserWithEmailAndPassword(email, senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = auth.currentUser

                                if (firebaseUser != null) {
                                    val user = User(
                                        id = firebaseUser.uid,
                                        nome = nome,
                                        email = email,
                                        pontos = 0,
                                        missoesConcluidas = 0,
                                        diasConsecutivos = 0
                                    )

                                    db.collection("usuarios")
                                        .document(firebaseUser.uid)
                                        .set(user)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                navController.context,
                                                "Conta criada com sucesso!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            navController.navigate("login") {
                                                popUpTo("register") {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                navController.context,
                                                "Erro ao salvar os dados no Firestore.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                            } else {
                                Toast.makeText(
                                    navController.context,
                                    task.exception?.message
                                        ?: "Erro ao criar conta.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                onBackToLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("challenges") {
            ChallengesScreen(navController)
        }

        composable("progress") {
            ProgressScreen(navController)
        }

        composable("settings") {
            SettingsScreen(navController)
        }
    }
}