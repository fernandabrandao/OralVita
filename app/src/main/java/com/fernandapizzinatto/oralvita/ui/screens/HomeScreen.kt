package com.fernandapizzinatto.oralvita.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fernandapizzinatto.oralvita.ui.model.GameState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ============================================================
// PALETA DE CORES FIEL AO FIGMA
// ============================================================

private val FigmaBlue = Color(0xFF3B82F6)
private val FigmaCardBlueBackground = Color(0xFF5294F7)
private val FigmaBackground = Color(0xFFF4F8FC)
private val FigmaWhite = Color(0xFFFFFFFF)
private val FigmaTextBlack = Color(0xFF1E293B)
private val FigmaTextGrey = Color(0xFF64748B)
private val FigmaTextMuted = Color(0xFF94A3B8)
private val FigmaAccentGreen = Color(0xFF22C55E)
private val FigmaLightGreen = Color(0xFFE8F5E9)
private val FigmaBorder = Color(0xFFE2E8F0)
private val FigmaOrange = Color(0xFFF59E0B)
private val FigmaPurple = Color(0xFF8B5CF6)
private val FigmaCyan = Color(0xFF06B6D4)

// ============================================================
// FUNÇÕES PARA CALCULAR A SEQUÊNCIA
// ============================================================

private fun obterDataHojeHome(): String {

    return java.text.SimpleDateFormat(
        "yyyy-MM-dd",
        java.util.Locale.getDefault()
    ).format(java.util.Date())
}

private fun obterDataAnteriorHome(
    data: String
): String {

    val formatter =
        java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        )

    val calendar =
        java.util.Calendar.getInstance()

    calendar.time =
        formatter.parse(data) ?: java.util.Date()

    calendar.add(
        java.util.Calendar.DAY_OF_MONTH,
        -1
    )

    return formatter.format(calendar.time)
}

private fun calcularSequenciaHome(
    historico: Map<*, *>,
    dataInicial: String
): Int {

    var sequencia = 0
    var dataAtual = dataInicial

    while (true) {

        val dadosDoDia =
            historico[dataAtual] as? Map<*, *>

        val missoesDoDia =
            dadosDoDia
                ?.get("missoesConcluidas")
                ?.toString()
                ?.toIntOrNull()
                ?: 0

        if (missoesDoDia > 0) {

            sequencia++

            dataAtual =
                obterDataAnteriorHome(dataAtual)

        } else {

            break
        }
    }

    return sequencia
}

// ============================================================
// HOME
// ============================================================

@Composable
fun HomeScreen(
    navController: NavController
) {

    val auth =
        FirebaseAuth.getInstance()

    val db =
        FirebaseFirestore.getInstance()

    var nomeUsuario by remember {
        mutableStateOf("Usuário")
    }

    var pontosUsuario by remember {
        mutableIntStateOf(0)
    }

    var sequenciaUsuario by remember {
        mutableIntStateOf(0)
    }

    var missoesConcluidas by remember {

        mutableStateOf(
            listOf(
                false,
                false,
                false,
                false
            )
        )
    }

    // ========================================================
    // CARREGA OS DADOS DO USUÁRIO DO FIREBASE
    // ========================================================

    LaunchedEffect(Unit) {

        auth.currentUser?.let { user ->

            db.collection("usuarios")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->

                    // ====================================================
                    // NOME
                    // ====================================================

                    nomeUsuario =
                        document.getString("nome")
                            ?: "Usuário"

                    // ====================================================
                    // PONTOS
                    // ====================================================

                    pontosUsuario =
                        document
                            .getLong("pontos")
                            ?.toInt()
                            ?: 0

                    // ====================================================
                    // MISSÕES INDIVIDUAIS
                    // ====================================================

                    val missoesFirebase =
                        document.get("missoes")
                                as? List<*>

                    missoesConcluidas =

                        if (missoesFirebase != null) {

                            List(4) { index ->

                                missoesFirebase
                                    .getOrNull(index)
                                        as? Boolean
                                    ?: false
                            }

                        } else {

                            listOf(
                                false,
                                false,
                                false,
                                false
                            )
                        }

                    // ====================================================
                    // MANTÉM O GAMESTATE SINCRONIZADO
                    // ====================================================

                    GameState.points =
                        pontosUsuario

                    GameState.completedMissions =
                        missoesConcluidas.count { concluida ->
                            concluida
                        }

                    // ====================================================
                    // SEQUÊNCIA REAL PELO HISTÓRICO
                    // ====================================================

                    val historicoFirebase =
                        document
                            .get("historicoDiario")
                                as? Map<*, *>

                    if (historicoFirebase != null) {

                        val hoje =
                            obterDataHojeHome()

                        sequenciaUsuario =
                            calcularSequenciaHome(
                                historico =
                                    historicoFirebase,
                                dataInicial =
                                    hoje
                            )

                        GameState.streak =
                            sequenciaUsuario

                    } else {

                        sequenciaUsuario = 0

                        GameState.streak = 0
                    }
                }
        }
    }

    // ========================================================
    // QUANTIDADE REAL DE MISSÕES CONCLUÍDAS
    // ========================================================

    val quantidadeMissoesConcluidas =
        missoesConcluidas.count { concluida ->
            concluida
        }

    // ========================================================
    // TELA
    // ========================================================

    Scaffold(

        bottomBar = {
            FigmaBottomNavigation(
                navController
            )
        },

        containerColor =
            FigmaBackground

    ) { innerPadding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .background(
                    FigmaBackground
                )

        ) {

            // ====================================================
            // 1. TOPO AZUL
            // ====================================================

            FigmaHeader(
                nome =
                    nomeUsuario,

                pontos =
                    pontosUsuario,

                missoesConcluidas =
                    quantidadeMissoesConcluidas
            )

            Column(

                modifier =
                    Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 20.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(16.dp)

            ) {

                // ====================================================
                // 2. SEQUÊNCIA E TOTAL
                // ====================================================

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)

                ) {

                    FigmaStatCard(

                        icon = "🔥",

                        title =
                            "SEQUÊNCIA",

                        value =
                            "$sequenciaUsuario dias",

                        subtext =
                            "Continue assim! 🔥",

                        modifier =
                            Modifier.weight(1f)
                    )

                    FigmaStatCard(

                        icon = "⭐",

                        title =
                            "TOTAL",

                        value =
                            "$pontosUsuario",

                        subtext =
                            "pontos acumulados",

                        modifier =
                            Modifier.weight(1f)
                    )
                }

                // ====================================================
                // 3. CARD DICA DO DIA
                // ====================================================

                FigmaTipCard(
                    dicaText =
                        "Troque a escova a cada 3 meses!"
                )

                // ====================================================
                // 4. MISSÕES DE HOJE
                // ====================================================

                Text(

                    text =
                        "MISSÕES DE HOJE",

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        FigmaTextGrey,

                    modifier =
                        Modifier.padding(
                            top = 8.dp
                        )
                )

                // ====================================================
                // MISSÃO 1
                // ====================================================

                FigmaMissionItem(

                    icon =
                        Icons.Default.WbSunny,

                    iconBgColor =
                        FigmaOrange.copy(
                            alpha = 0.15f
                        ),

                    iconTint =
                        FigmaOrange,

                    text =
                        "Escovar de manhã",

                    isCompleted =
                        missoesConcluidas
                            .getOrElse(0) {
                                false
                            }
                )

                // ====================================================
                // MISSÃO 2
                // ====================================================

                FigmaMissionItem(

                    icon =
                        Icons.Default.AccessTime,

                    iconBgColor =
                        FigmaCyan.copy(
                            alpha = 0.15f
                        ),

                    iconTint =
                        FigmaCyan,

                    text =
                        "2 minutos de escovação",

                    isCompleted =
                        missoesConcluidas
                            .getOrElse(1) {
                                false
                            }
                )

                // ====================================================
                // MISSÃO 3
                // ====================================================

                FigmaMissionItem(

                    icon =
                        Icons.Outlined.Air,

                    iconBgColor =
                        FigmaAccentGreen.copy(
                            alpha = 0.15f
                        ),

                    iconTint =
                        FigmaAccentGreen,

                    text =
                        "Usar fio dental",

                    isCompleted =
                        missoesConcluidas
                            .getOrElse(2) {
                                false
                            }
                )

                // ====================================================
                // MISSÃO 4
                // ====================================================

                FigmaMissionItem(

                    icon =
                        Icons.Outlined.NightsStay,

                    iconBgColor =
                        FigmaPurple.copy(
                            alpha = 0.15f
                        ),

                    iconTint =
                        FigmaPurple,

                    text =
                        "Escovar antes de dormir",

                    isCompleted =
                        missoesConcluidas
                            .getOrElse(3) {
                                false
                            }
                )

                // ====================================================
                // 5. BOTÃO VER TODOS OS DESAFIOS
                // ====================================================

                Button(

                    onClick = {

                        navController.navigate(
                            "challenges"
                        )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                FigmaAccentGreen
                        ),

                    shape =
                        RoundedCornerShape(16.dp)

                ) {

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        Row(

                            verticalAlignment =
                                Alignment.CenterVertically

                        ) {

                            Text(
                                "✨",
                                fontSize =
                                    18.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Text(

                                text =
                                    "Ver Todos os Desafios",

                                fontSize =
                                    16.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    FigmaWhite
                            )
                        }

                        Icon(

                            imageVector =
                                Icons.Default.ChevronRight,

                            contentDescription =
                                null,

                            tint =
                                FigmaWhite
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// CABEÇALHO
// ============================================================

@Composable
private fun FigmaHeader(
    nome: String,
    pontos: Int,
    missoesConcluidas: Int
) {

    Box(

        modifier = Modifier
            .fillMaxWidth()
            .background(

                color =
                    FigmaBlue,

                shape =
                    RoundedCornerShape(
                        bottomStart = 32.dp,
                        bottomEnd = 32.dp
                    )
            )
            .padding(
                horizontal = 20.dp
            )
            .padding(
                top = 20.dp,
                bottom = 24.dp
            )

    ) {

        Column {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.Top

            ) {

                Column(

                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(
                                end = 8.dp
                            )

                ) {

                    Text(

                        text =
                            "${getSaudacao()}, $nome! 👋",

                        color =
                            FigmaWhite,

                        fontSize =
                            20.sp,

                        fontWeight =
                            FontWeight.Bold,

                        lineHeight =
                            24.sp
                    )
                }

                // ====================================================
                // BOTÃO SOM
                // ====================================================

                IconButton(

                    onClick = { },

                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            FigmaWhite.copy(
                                alpha = 0.2f
                            ),
                            CircleShape
                        )

                ) {

                    Icon(

                        imageVector =
                            Icons.AutoMirrored.Outlined.VolumeUp,

                        contentDescription =
                            "Áudio",

                        tint =
                            FigmaWhite,

                        modifier =
                            Modifier.size(18.dp)
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(

                text =
                    "Vamos cuidar do seu sorriso hoje?",

                color =
                    FigmaWhite,

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                lineHeight =
                    28.sp
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            // ====================================================
            // CARD DE RESUMO
            // ====================================================

            FigmaHeaderSummaryCard(

                pontos =
                    pontos,

                missoesConcluidas =
                    missoesConcluidas
            )
        }
    }
}

// ============================================================
// CARD DE RESUMO
// ============================================================

@Composable
private fun FigmaHeaderSummaryCard(
    pontos: Int,
    missoesConcluidas: Int
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    FigmaCardBlueBackground
            )

    ) {

        Column(

            modifier =
                Modifier.padding(18.dp)

        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween

            ) {

                Text(

                    "Pontos hoje",

                    fontSize =
                        12.sp,

                    color =
                        FigmaWhite.copy(
                            alpha = 0.8f
                        )
                )

                Text(

                    "Missões",

                    fontSize =
                        12.sp,

                    color =
                        FigmaWhite.copy(
                            alpha = 0.8f
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.Bottom

            ) {

                Text(

                    "$pontos",

                    fontSize =
                        36.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        FigmaWhite
                )

                Text(

                    "$missoesConcluidas/4",

                    fontSize =
                        36.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        FigmaWhite
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            val progress =
                (
                        missoesConcluidas
                            .toFloat() / 4f
                        ).coerceIn(
                        0f,
                        1f
                    )

            LinearProgressIndicator(

                progress = {
                    progress
                },

                color =
                    FigmaWhite,

                trackColor =
                    FigmaWhite.copy(
                        alpha = 0.3f
                    ),

                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(
                        RoundedCornerShape(4.dp)
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            val restando =
                (
                        4 - missoesConcluidas
                        ).coerceAtLeast(0)

            Text(

                text =
                    if (restando > 0) {

                        "Faltam $restando missões para completar o dia!"

                    } else {

                        "Todas as missões do dia foram concluídas! 🎉"
                    },

                fontSize =
                    11.sp,

                color =
                    FigmaWhite.copy(
                        alpha = 0.9f
                    )
            )
        }
    }
}

// ============================================================
// CARD DE ESTATÍSTICA
// ============================================================

@Composable
private fun FigmaStatCard(
    icon: String,
    title: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {

    Card(

        modifier =
            modifier.border(
                1.dp,
                FigmaBorder,
                RoundedCornerShape(16.dp)
            ),

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    FigmaWhite
            )
    ) {

        Column(

            modifier =
                Modifier.padding(14.dp)

        ) {

            Row(

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Text(
                    icon,
                    fontSize =
                        14.sp
                )

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Text(

                    title,

                    fontSize =
                        10.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        FigmaTextGrey
                )
            }

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(

                value,

                fontSize =
                    22.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    FigmaTextBlack
            )

            Text(

                subtext,

                fontSize =
                    10.sp,

                color =
                    FigmaTextMuted
            )
        }
    }
}

// ============================================================
// CARD DICA
// ============================================================

@Composable
private fun FigmaTipCard(
    dicaText: String =
        "Troque a escova a cada 3 meses!"
) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .border(

                1.dp,

                FigmaAccentGreen.copy(
                    alpha = 0.3f
                ),

                RoundedCornerShape(16.dp)
            ),

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    FigmaLightGreen
            )
    ) {

        Row(

            modifier =
                Modifier.padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Text(
                "🪥",
                fontSize =
                    28.sp
            )

            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )

            Column {

                Text(

                    "Dica do dia 💡",

                    fontSize =
                        12.sp,

                    color =
                        FigmaTextGrey,

                    fontWeight =
                        FontWeight.Medium
                )

                Text(

                    dicaText,

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        FigmaTextBlack
                )
            }
        }
    }
}

// ============================================================
// ITEM DE MISSÃO
// ============================================================

@Composable
private fun FigmaMissionItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    text: String,
    isCompleted: Boolean = false
) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .border(

                width = 1.dp,

                color =
                    if (isCompleted) {

                        FigmaAccentGreen.copy(
                            alpha = 0.5f
                        )

                    } else {

                        FigmaBorder
                    },

                shape =
                    RoundedCornerShape(16.dp)
            ),

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    if (isCompleted) {

                        FigmaLightGreen.copy(
                            alpha = 0.5f
                        )

                    } else {

                        FigmaWhite
                    }
            )
    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceBetween

        ) {

            Row(

                verticalAlignment =
                    Alignment.CenterVertically,

                modifier =
                    Modifier.weight(1f)

            ) {

                Box(

                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            iconBgColor,
                            CircleShape
                        ),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Icon(

                        imageVector =
                            icon,

                        contentDescription =
                            null,

                        tint =
                            iconTint,

                        modifier =
                            Modifier.size(20.dp)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Text(

                    text =
                        text,

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        if (isCompleted) {

                            FigmaTextGrey

                        } else {

                            FigmaTextBlack
                        }
                )
            }

            Icon(

                imageVector =
                    if (isCompleted) {

                        Icons.Outlined.CheckCircle

                    } else {

                        Icons.Outlined.RadioButtonUnchecked
                    },

                contentDescription =
                    null,

                tint =
                    if (isCompleted) {

                        FigmaAccentGreen

                    } else {

                        FigmaTextMuted
                    },

                modifier =
                    Modifier.size(22.dp)
            )
        }
    }
}

// ============================================================
// NAVEGAÇÃO INFERIOR
// ============================================================

@Composable
private fun FigmaBottomNavigation(
    navController: NavController
) {

    NavigationBar(

        containerColor =
            FigmaWhite,

        tonalElevation =
            8.dp

    ) {

        // ====================================================
        // INÍCIO
        // ====================================================

        NavigationBarItem(

            selected =
                true,

            onClick = { },

            icon = {

                Text(
                    "🏠",
                    fontSize =
                        18.sp
                )
            },

            label = {

                Text(

                    "Início",

                    fontSize =
                        11.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            },

            colors =
                NavigationBarItemDefaults.colors(

                    indicatorColor =
                        FigmaBlue.copy(
                            alpha = 0.1f
                        ),

                    selectedTextColor =
                        FigmaBlue
                )
        )

        // ====================================================
        // DESAFIOS
        // ====================================================

        NavigationBarItem(

            selected =
                false,

            onClick = {

                navController.navigate(
                    "challenges"
                )
            },

            icon = {

                Text(
                    "✨",
                    fontSize =
                        18.sp
                )
            },

            label = {

                Text(

                    "Desafios",

                    fontSize =
                        11.sp
                )
            }
        )

        // ====================================================
        // PROGRESSO
        // ====================================================

        NavigationBarItem(

            selected =
                false,

            onClick = {

                navController.navigate(
                    "progress"
                )
            },

            icon = {

                Text(
                    "📊",
                    fontSize =
                        18.sp
                )
            },

            label = {

                Text(

                    "Progresso",

                    fontSize =
                        11.sp
                )
            }
        )
    }
}

// ============================================================
// SAUDAÇÃO
// ============================================================

private fun getSaudacao(): String {

    val hora =
        java.util.Calendar
            .getInstance()
            .get(
                java.util.Calendar.HOUR_OF_DAY
            )

    return when (hora) {

        in 5..11 ->
            "Bom dia"

        in 12..17 ->
            "Boa tarde"

        else ->
            "Boa noite"
    }
}