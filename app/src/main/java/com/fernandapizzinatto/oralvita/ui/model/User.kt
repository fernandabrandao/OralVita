package com.fernandapizzinatto.oralvita.ui.model

data class User(

    val id: String = "",

    val nome: String = "",

    val email: String = "",

    val pontos: Int = 0,

    val missoesConcluidas: Int = 0,

    val diasConsecutivos: Int = 7,

    val missoes: List<Boolean> = listOf(
        false,
        false,
        false,
        false
    )

)