package com.fernandapizzinatto.oralvita.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// =========================================================
// CORES DO TEMA FIGMA - PROGRESSO
// =========================================================

private val FigmaBackground = Color(0xFFF0F6FC)
private val FigmaWhite = Color(0xFFFFFFFF)
private val FigmaHeaderGreen = Color(0xFF2EA97D)
private val FigmaHeaderLightGreen = Color(0xFF38B287)
private val FigmaTextBlack = Color(0xFF1E293B)
private val FigmaTextGrey = Color(0xFF64748B)
private val FigmaTextMuted = Color(0xFF94A3B8)
private val FigmaPrimaryBlue = Color(0xFF5294F7)
private val FigmaProgressBlue = Color(0xFF38BDF8)
private val FigmaBorder = Color(0xFFE2E8F0)
private val FigmaAchievementBorder = Color(0xFFA7F3D0)
private val FigmaAchievementBg = Color(0xFFECFDF5)


// =========================================================
// FUNÇÕES AUXILIARES DE DATA
// =========================================================

private fun obterDataHoje(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(Date())
}

private fun obterDataComDeslocamento(
    dataInicial: String,
    quantidadeDias: Int
): String {

    val formatter = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    )

    val calendar = Calendar.getInstance()

    calendar.time =
        formatter.parse(dataInicial) ?: Date()

    calendar.add(
        Calendar.DAY_OF_MONTH,
        quantidadeDias
    )

    return formatter.format(calendar.time)
}

private fun obterDataAnterior(
    data: String
): String {
    return obterDataComDeslocamento(
        dataInicial = data,
        quantidadeDias = -1
    )
}

private fun quantidadeMissoesDoDia(
    historico: Map<String, Any>,
    data: String
): Int {

    val dadosDoDia =
        historico[data] as? Map<*, *>
            ?: return 0

    return dadosDoDia
        .get("missoesConcluidas")
        ?.toString()
        ?.toIntOrNull()
        ?: 0
}


private fun contarProcessosNoPeriodo(
    historico: Map<String, Any>,
    datas: List<String>,
    campo: String
): Int {

    return datas.count { data ->
        val dadosDoDia =
            historico[data] as? Map<*, *>

        dadosDoDia?.get(campo) as? Boolean == true
    }
}

private fun obterStreakReal(
    historico: Map<String, Any>,
    dataInicial: String
): Int {

    var streak = 0
    var dataAtual = dataInicial

    while (true) {

        val missoes =
            quantidadeMissoesDoDia(
                historico = historico,
                data = dataAtual
            )

        if (missoes > 0) {

            streak++

            dataAtual =
                obterDataAnterior(dataAtual)

        } else {

            break
        }
    }

    return streak
}

private fun obterInicioDaSemana(
    dataHoje: String
): String {

    val formatter =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

    val calendar = Calendar.getInstance()

    calendar.time =
        formatter.parse(dataHoje) ?: Date()

    // Segunda-feira = primeiro dia da semana
    val diaSemana =
        calendar.get(Calendar.DAY_OF_WEEK)

    val diasDesdeSegunda =
        when (diaSemana) {
            Calendar.SUNDAY -> 6
            else -> diaSemana - Calendar.MONDAY
        }

    calendar.add(
        Calendar.DAY_OF_MONTH,
        -diasDesdeSegunda
    )

    return formatter.format(calendar.time)
}

private fun obterDiasDoMesAtual(
    dataHoje: String
): Int {

    val formatter =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

    val calendar = Calendar.getInstance()

    calendar.time =
        formatter.parse(dataHoje) ?: Date()

    return calendar.getActualMaximum(
        Calendar.DAY_OF_MONTH
    )
}

private fun obterNumeroDeDiasDoMes(
    dataHoje: String
): Int {

    val formatter =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

    val calendar = Calendar.getInstance()

    calendar.time =
        formatter.parse(dataHoje) ?: Date()

    return calendar.getActualMaximum(
        Calendar.DAY_OF_MONTH
    )
}

private fun mesmaCompetenciaMensal(
    data: String,
    dataHoje: String
): Boolean {

    return data.substring(0, 7) ==
            dataHoje.substring(0, 7)
}


// =========================================================
// TELA DE PROGRESSO
// =========================================================

@Composable
fun ProgressScreen(
    navController: NavController
) {

    val auth =
        FirebaseAuth.getInstance()

    val db =
        FirebaseFirestore.getInstance()

    var pontos by remember {
        mutableStateOf(0)
    }

    var missoesConcluidas by remember {
        mutableStateOf(0)
    }

    var streak by remember {
        mutableStateOf(0)
    }

    // 0 = Escovar de manhã
    // 1 = 2 minutos de escovação
    // 2 = Usar fio dental
    // 3 = Escovar antes de dormir
    var missoesRealizadas by remember {
        mutableStateOf(
            listOf(
                false,
                false,
                false,
                false
            )
        )
    }

    // Histórico diário vindo do Firebase.
    var historicoDiario by remember {
        mutableStateOf(
            emptyMap<String, Any>()
        )
    }

    var conquistaSelecionada by remember {
        mutableStateOf<Int?>(null)
    }

    val totalMissoes = 4

    val dataHoje =
        remember {
            obterDataHoje()
        }


    // =====================================================
    // LEITURA DO FIREBASE
    // =====================================================

    LaunchedEffect(Unit) {

        auth.currentUser?.let { user ->

            db.collection("usuarios")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->

                    // -----------------------------
                    // Pontos totais
                    // -----------------------------

                    pontos =
                        document
                            .getLong("pontos")
                            ?.toInt()
                            ?: 0


                    // -----------------------------
                    // Missões do dia
                    // -----------------------------

                    missoesConcluidas =
                        document
                            .getLong("missoesConcluidas")
                            ?.toInt()
                            ?: 0


                    // -----------------------------
                    // Missões individuais do dia
                    // -----------------------------

                    val missoes =
                        document.get("missoes")
                                as? List<*>

                    if (missoes != null) {

                        missoesRealizadas =
                            List(totalMissoes) { index ->

                                missoes
                                    .getOrNull(index)
                                        as? Boolean
                                    ?: false
                            }
                    }


                    // -----------------------------
                    // Histórico diário
                    // -----------------------------

                    val historico =
                        document.get(
                            "historicoDiario"
                        ) as? Map<*, *>

                    if (historico != null) {

                        val historicoCorrigido =
                            mutableMapOf<String, Any>()

                        historico.forEach { (chave, valor) ->

                            if (
                                chave != null &&
                                valor != null
                            ) {

                                historicoCorrigido[
                                    chave.toString()
                                ] = valor
                            }
                        }

                        historicoDiario =
                            historicoCorrigido


                        // Calcula o streak real
                        // usando o histórico.

                        streak =
                            obterStreakReal(
                                historico =
                                    historicoCorrigido,
                                dataInicial =
                                    dataHoje
                            )
                    } else {

                        // Mantém o valor salvo caso
                        // ainda não exista histórico.

                        streak =
                            document
                                .getLong("streak")
                                ?.toInt()
                                ?: 0
                    }
                }
        }
    }


    // =====================================================
    // CÁLCULOS DO PROGRESSO SEMANAL
    // =====================================================

    val inicioSemana =
        obterInicioDaSemana(
            dataHoje
        )

    val datasSemana =
        List(7) { index ->
            obterDataComDeslocamento(
                dataInicial = inicioSemana,
                quantidadeDias = index
            )
        }

    val escovacaoManhaSemana =
        contarProcessosNoPeriodo(
            historicoDiario,
            datasSemana,
            "escovarManha"
        )

    val escovacaoDoisMinutosSemana =
        contarProcessosNoPeriodo(
            historicoDiario,
            datasSemana,
            "doisMinutos"
        )

    val fioDentalSemana =
        contarProcessosNoPeriodo(
            historicoDiario,
            datasSemana,
            "fioDental"
        )

    val escovacaoNoiteSemana =
        contarProcessosNoPeriodo(
            historicoDiario,
            datasSemana,
            "escovarNoite"
        )

    val missoesSemana =
        datasSemana.sumOf { data ->

            quantidadeMissoesDoDia(
                historico =
                    historicoDiario,
                data = data
            )
        }

    val totalMissoesSemana =
        7 * totalMissoes

    val progressoSemanal =
        (
                missoesSemana.toFloat() /
                        totalMissoesSemana.toFloat()
                ).coerceIn(
                0f,
                1f
            )


    // =====================================================
    // CÁLCULOS DO PROGRESSO MENSAL
    // =====================================================

    val diasNoMes =
        obterNumeroDeDiasDoMes(
            dataHoje
        )

    val totalMissoesMes =
        diasNoMes * totalMissoes

    val missoesMes =
        historicoDiario
            .filter { (data, _) ->
                mesmaCompetenciaMensal(
                    data = data,
                    dataHoje = dataHoje
                )
            }
            .values
            .sumOf { valor ->

                val dados =
                    valor as? Map<*, *>

                dados
                    ?.get("missoesConcluidas")
                    ?.toString()
                    ?.toIntOrNull()
                    ?: 0
            }

    val progressoMensal =
        (
                missoesMes.toFloat() /
                        totalMissoesMes.toFloat()
                ).coerceIn(
                0f,
                1f
            )


    // =====================================================
    // TOTAL DE MISSÕES JÁ REALIZADAS
    // =====================================================

    val totalHistoricoMissoes =
        historicoDiario.values.sumOf { valor ->

            val dados =
                valor as? Map<*, *>

            dados
                ?.get("missoesConcluidas")
                ?.toString()
                ?.toIntOrNull()
                ?: 0
        }


    // =====================================================
    // INTERFACE
    // =====================================================

    Scaffold(

        bottomBar = {
            FigmaBottomNavigation(
                navController
            )
        },

        containerColor =
            FigmaBackground

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    FigmaBackground
                )
                .verticalScroll(
                    rememberScrollState()
                )

        ) {

            // =================================================
            // 1. CABEÇALHO
            // =================================================

            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 32.dp,
                            bottomEnd = 32.dp
                        )
                    )
                    .background(
                        FigmaHeaderGreen
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 24.dp
                    )

            ) {

                Column {

                    IconButton(

                        onClick = {
                            navController.popBackStack()
                        },

                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                FigmaHeaderLightGreen,
                                CircleShape
                            )

                    ) {

                        Icon(

                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,

                            contentDescription =
                                "Voltar",

                            tint =
                                FigmaWhite
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        Text(

                            text =
                                "Meu Progresso",

                            fontSize =
                                22.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                FigmaWhite
                        )

                        IconButton(

                            onClick = {
                                navController.navigate(
                                    "settings"
                                )
                            },

                            modifier =
                                Modifier
                                    .size(42.dp)
                                    .background(
                                        FigmaHeaderLightGreen,
                                        CircleShape
                                    )

                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Settings,

                                contentDescription =
                                    "Configurações",

                                tint =
                                    FigmaWhite,

                                modifier =
                                    Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(

                        text =
                            "Total acumulado",

                        fontSize =
                            13.sp,

                        color =
                            FigmaWhite.copy(
                                alpha = 0.8f
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.Bottom
                    ) {

                        Text(

                            text =
                                "$pontos",

                            fontSize =
                                38.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            color =
                                FigmaWhite
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Text(

                            text =
                                "pontos",

                            fontSize =
                                16.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                FigmaWhite.copy(
                                    alpha = 0.9f
                                ),

                            modifier =
                                Modifier.padding(
                                    bottom = 6.dp
                                )
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            Column(

                modifier =
                    Modifier.padding(
                        horizontal = 16.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(16.dp)

            ) {


                // =================================================
                // 2. MÉTRICAS
                // =================================================

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)

                ) {

                    MetricCard(

                        icon =
                            Icons.Default.LocalFireDepartment,

                        iconColor =
                            Color(0xFFF97316),

                        value =
                            "$streak",

                        label =
                            "dias seguidos",

                        modifier =
                            Modifier.weight(1f)
                    )

                    MetricCard(

                        icon =
                            Icons.Default.CheckCircleOutline,

                        iconColor =
                            Color(0xFF10B981),

                        value =
                            "$missoesConcluidas/$totalMissoes",

                        label =
                            "hoje",

                        modifier =
                            Modifier.weight(1f)
                    )

                    MetricCard(

                        icon =
                            Icons.Default.StarOutline,

                        iconColor =
                            Color(0xFF3B82F6),

                        value =
                            "${missoesConcluidas * 20}",

                        label =
                            "pts hoje",

                        modifier =
                            Modifier.weight(1f)
                    )
                }


                // =================================================
                // 3. PROGRESSO DIÁRIO
                // =================================================

                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(20.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                FigmaWhite
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
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically

                        ) {

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Text(
                                    "☀️",
                                    fontSize =
                                        16.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(8.dp)
                                )

                                Text(

                                    text =
                                        "Progresso Diário",

                                    fontSize =
                                        15.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        FigmaTextBlack
                                )
                            }

                            val progressPercent =
                                (
                                        missoesConcluidas.toFloat() /
                                                totalMissoes.toFloat()
                                        ) * 100

                            Text(

                                text =
                                    "${progressPercent.toInt()}%",

                                fontSize =
                                    13.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    FigmaTextGrey
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        LinearProgressIndicator(

                            progress = {

                                (
                                        missoesConcluidas.toFloat() /
                                                totalMissoes.toFloat()
                                        ).coerceIn(
                                        0f,
                                        1f
                                    )
                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            5.dp
                                        )
                                    ),

                            color =
                                FigmaProgressBlue,

                            trackColor =
                                Color(0xFFE2E8F0)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceEvenly

                        ) {

                            MiniIconCircle(
                                icon =
                                    Icons.Default.WbSunny,
                                isActive =
                                    missoesRealizadas
                                        .getOrNull(0)
                                            == true
                            )

                            MiniIconCircle(
                                icon =
                                    Icons.Default.AccessTime,
                                isActive =
                                    missoesRealizadas
                                        .getOrNull(1)
                                            == true
                            )

                            MiniIconCircle(
                                icon =
                                    Icons.Default.Air,
                                isActive =
                                    missoesRealizadas
                                        .getOrNull(2)
                                            == true
                            )

                            MiniIconCircle(
                                icon =
                                    Icons.Default.Nightlight,
                                isActive =
                                    missoesRealizadas
                                        .getOrNull(3)
                                            == true
                            )
                        }
                    }
                }


                // =================================================
                // 4. PROGRESSO SEMANAL
                // =================================================

                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(20.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                FigmaWhite
                        )

                ) {

                    Column(

                        modifier =
                            Modifier.padding(18.dp)

                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.CalendarToday,

                                contentDescription =
                                    null,

                                tint =
                                    FigmaHeaderGreen,

                                modifier =
                                    Modifier.size(18.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Text(

                                text =
                                    "Progresso Semanal",

                                fontSize =
                                    15.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    FigmaTextBlack
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        LinearProgressIndicator(

                            progress = {
                                progressoSemanal
                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            5.dp
                                        )
                                    ),

                            color =
                                FigmaProgressBlue,

                            trackColor =
                                Color(0xFFE2E8F0)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(

                            text =
                                "$missoesSemana de $totalMissoesSemana missões semanais",

                            fontSize =
                                12.sp,

                            color =
                                FigmaTextGrey
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween

                        ) {

                            val days =
                                listOf(
                                    "Seg",
                                    "Ter",
                                    "Qua",
                                    "Qui",
                                    "Sex",
                                    "Sáb",
                                    "Dom"
                                )

                            days.forEachIndexed { idx, day ->

                                val dataDoDia =
                                    datasSemana[idx]

                                val teveMissao =
                                    quantidadeMissoesDoDia(
                                        historico =
                                            historicoDiario,
                                        data =
                                            dataDoDia
                                    ) > 0

                                Column(

                                    horizontalAlignment =
                                        Alignment.CenterHorizontally

                                ) {

                                    Box(

                                        modifier =
                                            Modifier
                                                .size(36.dp)
                                                .clip(
                                                    CircleShape
                                                )
                                                .background(

                                                    if (teveMissao)
                                                        FigmaPrimaryBlue
                                                    else
                                                        Color(0xFFE2E8F0)
                                                )
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(4.dp)
                                    )

                                    Text(

                                        text =
                                            day,

                                        fontSize =
                                            11.sp,

                                        color =
                                            FigmaTextGrey,

                                        fontWeight =
                                            FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }


                // =================================================
                // 5. PROGRESSO MENSAL
                // =================================================

                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(20.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                FigmaWhite
                        )

                ) {

                    Column(

                        modifier =
                            Modifier.padding(18.dp)

                    ) {

                        Row(

                            verticalAlignment =
                                Alignment.CenterVertically

                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.CalendarToday,

                                contentDescription =
                                    null,

                                tint =
                                    Color(0xFFA78BFA),

                                modifier =
                                    Modifier.size(18.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Text(

                                text =
                                    "Progresso Mensal",

                                fontSize =
                                    15.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    FigmaTextBlack
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        LinearProgressIndicator(

                            progress = {
                                progressoMensal
                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            5.dp
                                        )
                                    ),

                            color =
                                FigmaProgressBlue,

                            trackColor =
                                Color(0xFFE2E8F0)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(

                            text =
                                "$missoesMes de $totalMissoesMes missões mensais",

                            fontSize =
                                12.sp,

                            color =
                                FigmaTextGrey
                        )
                    }
                }

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
                            text = "Processos de Higiene Bucal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaTextBlack
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Conclusões registradas nesta semana",
                            fontSize = 12.sp,
                            color = FigmaTextGrey
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        val processos = listOf(
                            Triple(
                                "Escovação da manhã",
                                escovacaoManhaSemana,
                                Color(0xFFEAB308)
                            ),
                            Triple(
                                "Escovação por 2 minutos",
                                escovacaoDoisMinutosSemana,
                                Color(0xFF38BDF8)
                            ),
                            Triple(
                                "Uso do fio dental",
                                fioDentalSemana,
                                Color(0xFF34D399)
                            ),
                            Triple(
                                "Escovação noturna",
                                escovacaoNoiteSemana,
                                Color(0xFFA78BFA)
                            )
                        )

                        processos.forEach { (nome, quantidade, cor) ->
                            Column(
                                modifier = Modifier.padding(
                                    bottom = 14.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = nome,
                                        fontSize = 13.sp,
                                        color = FigmaTextBlack
                                    )

                                    Text(
                                        text = "$quantidade/7 dias",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FigmaTextGrey
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = {
                                        quantidade / 7f
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = cor,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }
                        }

                        Text(
                            text = "Os valores representam dias com conclusão registrada no histórico.",
                            fontSize = 11.sp,
                            color = FigmaTextGrey
                        )
                    }
                }

                // =================================================
                // 6. CONQUISTAS
                // =================================================

                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(20.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                FigmaWhite
                        )

                ) {

                    Column(

                        modifier =
                            Modifier.padding(18.dp)

                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(

                                text =
                                    "Conquistas",

                                fontSize =
                                    16.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    FigmaTextBlack
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )

                            Icon(

                                imageVector =
                                    Icons.Default.EmojiEvents,

                                contentDescription =
                                    null,

                                tint =
                                    Color(0xFFEAB308),

                                modifier =
                                    Modifier.size(20.dp)
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Column(

                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)

                        ) {

                            Row(

                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)

                            ) {

                                AchievementItem(

                                    title =
                                        "Primeiro\nSorriso",

                                    icon =
                                        "🦷",

                                    isUnlocked =
                                        totalHistoricoMissoes >= 1,

                                    modifier =
                                        Modifier.weight(1f),

                                    onClick = {
                                        conquistaSelecionada = 0
                                    }
                                )

                                AchievementItem(

                                    title =
                                        "3 Dias\nSeguidos",

                                    icon =
                                        "🌟",

                                    isUnlocked =
                                        streak >= 3,

                                    modifier =
                                        Modifier.weight(1f),

                                    onClick = {
                                        conquistaSelecionada = 1
                                    }
                                )

                                AchievementItem(

                                    title =
                                        "Semana\nPerfeita",

                                    icon =
                                        "🔥",

                                    isUnlocked =
                                        streak >= 7,

                                    modifier =
                                        Modifier.weight(1f),

                                    onClick = {
                                        conquistaSelecionada = 2
                                    }
                                )
                            }

                            Row(

                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)

                            ) {

                                AchievementItem(

                                    title =
                                        "Mês\nCompleto",

                                    icon =
                                        "💎",

                                    isUnlocked =
                                        streak >= 30,

                                    modifier =
                                        Modifier.weight(1f),

                                    onClick = {
                                        conquistaSelecionada = 3
                                    }
                                )

                                AchievementItem(

                                    title =
                                        "Campeão\nBucal",

                                    icon =
                                        "🏆",

                                    isUnlocked =
                                        pontos >= 200,

                                    modifier =
                                        Modifier.weight(1f),

                                    onClick = {
                                        conquistaSelecionada = 4
                                    }
                                )

                                AchievementItem(

                                    title =
                                        "100% do\nDia",

                                    icon =
                                        "🎯",

                                    isUnlocked =
                                        missoesConcluidas >= 4,

                                    modifier =
                                        Modifier.weight(1f),

                                    onClick = {
                                        conquistaSelecionada = 5
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )
            }
        }
    }


    // =========================================================
    // DIÁLOGO DAS CONQUISTAS
    // =========================================================

    conquistaSelecionada?.let { conquista ->

        val titulo: String
        val icone: String
        val descricao: String
        val progresso: String
        val concluida: Boolean
        val restante: String?

        when (conquista) {

            // -------------------------------------------------
            // 0 - PRIMEIRO SORRISO
            // -------------------------------------------------

            0 -> {

                titulo =
                    "Primeiro Sorriso"

                icone =
                    "🦷"

                descricao =
                    "Complete sua primeira missão de higiene bucal."

                progresso =
                    "${if (totalHistoricoMissoes >= 1) 1 else 0}/1 missão"

                concluida =
                    totalHistoricoMissoes >= 1

                restante =
                    if (!concluida)
                        "Complete 1 missão para desbloquear."
                    else
                        null
            }


            // -------------------------------------------------
            // 1 - 3 DIAS SEGUIDOS
            // -------------------------------------------------

            1 -> {

                titulo =
                    "3 Dias Seguidos"

                icone =
                    "🌟"

                descricao =
                    "Mantenha sua rotina de higiene bucal por 3 dias seguidos."

                progresso =
                    "${streak.coerceAtMost(3)}/3 dias"

                concluida =
                    streak >= 3

                restante =
                    if (!concluida)
                        "Faltam ${3 - streak.coerceAtMost(3)} dias seguidos."
                    else
                        null
            }


            // -------------------------------------------------
            // 2 - SEMANA PERFEITA
            // -------------------------------------------------

            2 -> {

                titulo =
                    "Semana Perfeita"

                icone =
                    "🔥"

                descricao =
                    "Mantenha sua rotina de higiene bucal por 7 dias seguidos."

                progresso =
                    "${streak.coerceAtMost(7)}/7 dias"

                concluida =
                    streak >= 7

                restante =
                    if (!concluida)
                        "Faltam ${7 - streak.coerceAtMost(7)} dias seguidos."
                    else
                        null
            }


            // -------------------------------------------------
            // 3 - MÊS COMPLETO
            // -------------------------------------------------

            3 -> {

                titulo =
                    "Mês Completo"

                icone =
                    "💎"

                descricao =
                    "Mantenha sua rotina de higiene bucal por 30 dias seguidos."

                progresso =
                    "${streak.coerceAtMost(30)}/30 dias"

                concluida =
                    streak >= 30

                restante =
                    if (!concluida)
                        "Faltam ${30 - streak.coerceAtMost(30)} dias seguidos."
                    else
                        null
            }


            // -------------------------------------------------
            // 4 - CAMPEÃO BUCAL
            // -------------------------------------------------

            4 -> {

                titulo =
                    "Campeão Bucal"

                icone =
                    "🏆"

                descricao =
                    "Complete missões para acumular pontos e alcançar 200 pontos."

                progresso =
                    "${pontos.coerceAtMost(200)}/200 pontos"

                concluida =
                    pontos >= 200

                restante =
                    if (!concluida)
                        "Faltam ${200 - pontos.coerceAtMost(200)} pontos."
                    else
                        null
            }


            // -------------------------------------------------
            // 5 - 100% DO DIA
            // -------------------------------------------------

            else -> {

                titulo =
                    "100% do Dia"

                icone =
                    "🎯"

                descricao =
                    "Complete todas as 4 missões diárias de higiene bucal."

                progresso =
                    "$missoesConcluidas/4 missões"

                concluida =
                    missoesConcluidas >= 4

                restante =
                    if (!concluida)
                        "Complete todas as missões restantes do dia."
                    else
                        null
            }
        }


        AlertDialog(

            onDismissRequest = {
                conquistaSelecionada = null
            },

            title = {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            icone,

                        fontSize =
                            28.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.width(10.dp)
                    )

                    Text(
                        text =
                            titulo,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },

            text = {

                Column {

                    Text(

                        text =
                            descricao,

                        fontSize =
                            14.sp,

                        color =
                            FigmaTextGrey
                    )

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Text(

                        text =
                            "Progresso",

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            FigmaTextBlack
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(

                        text =
                            progresso,

                        fontSize =
                            15.sp,

                        color =
                            FigmaPrimaryBlue,

                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    // -----------------------------------------
                    // DETALHAMENTO DO 100% DO DIA
                    // -----------------------------------------

                    if (conquista == 5) {

                        Text(

                            text =
                                "☀️ Escovar de manhã " +
                                        if (
                                            missoesRealizadas
                                                .getOrNull(0)
                                            == true
                                        )
                                            "✅"
                                        else
                                            "⬜",

                            fontSize =
                                13.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(

                            text =
                                "⏱️ 2 minutos de escovação " +
                                        if (
                                            missoesRealizadas
                                                .getOrNull(1)
                                            == true
                                        )
                                            "✅"
                                        else
                                            "⬜",

                            fontSize =
                                13.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(

                            text =
                                "🧵 Usar fio dental " +
                                        if (
                                            missoesRealizadas
                                                .getOrNull(2)
                                            == true
                                        )
                                            "✅"
                                        else
                                            "⬜",

                            fontSize =
                                13.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(

                            text =
                                "🌙 Escovar antes de dormir " +
                                        if (
                                            missoesRealizadas
                                                .getOrNull(3)
                                            == true
                                        )
                                            "✅"
                                        else
                                            "⬜",

                            fontSize =
                                13.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )
                    }


                    if (concluida) {

                        Text(

                            text =
                                "🎉 Parabéns!",

                            fontSize =
                                16.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                FigmaHeaderGreen
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(

                            text =
                                "Você desbloqueou esta conquista!",

                            fontSize =
                                13.sp,

                            color =
                                FigmaTextGrey
                        )

                    } else {

                        Text(

                            text =
                                restante ?: "",

                            fontSize =
                                13.sp,

                            color =
                                FigmaTextGrey
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(

                    onClick = {
                        conquistaSelecionada = null
                    }

                ) {

                    Text(
                        "Continuar"
                    )
                }
            }
        )
    }
}


// =========================================================
// MÉTRICA RÁPIDA
// =========================================================

@Composable
private fun MetricCard(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {

    Card(

        modifier =
            modifier,

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
                Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center

        ) {

            Icon(

                imageVector =
                    icon,

                contentDescription =
                    null,

                tint =
                    iconColor,

                modifier =
                    Modifier.size(22.dp)
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(

                text =
                    value,

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    FigmaTextBlack,

                textAlign =
                    TextAlign.Center
            )

            Text(

                text =
                    label,

                fontSize =
                    11.sp,

                color =
                    FigmaTextGrey,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}


// =========================================================
// CÍRCULO DE ÍCONE DIÁRIO
// =========================================================

@Composable
private fun MiniIconCircle(
    icon: ImageVector,
    isActive: Boolean
) {

    Box(

        modifier =
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(

                    if (isActive)
                        Color(0xFFE0F2FE)
                    else
                        Color(0xFFF1F5F9)

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

                if (isActive)
                    FigmaProgressBlue
                else
                    FigmaTextMuted,

            modifier =
                Modifier.size(20.dp)
        )
    }
}


// =========================================================
// CONQUISTA
// =========================================================

@Composable
private fun AchievementItem(
    title: String,
    icon: String,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Box(

        modifier =
            modifier
                .height(105.dp)
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .background(

                    if (isUnlocked)
                        FigmaAchievementBg
                    else
                        Color(0xFFF8FAFC)

                )
                .border(

                    width =
                        1.dp,

                    color =

                        if (isUnlocked)
                            FigmaAchievementBorder
                        else
                            FigmaBorder,

                    shape =
                        RoundedCornerShape(16.dp)
                )
                .clickable {
                    onClick()
                },

        contentAlignment =
            Alignment.Center

    ) {

        Column(

            horizontalAlignment =
                Alignment.CenterHorizontally,

            modifier =
                Modifier.padding(6.dp)

        ) {

            Text(

                text =
                    icon,

                fontSize =
                    26.sp
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(

                text =
                    title,

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.SemiBold,

                color =

                    if (isUnlocked)
                        FigmaTextBlack
                    else
                        FigmaTextMuted,

                textAlign =
                    TextAlign.Center,

                lineHeight =
                    13.sp
            )
        }
    }
}


// =========================================================
// BARRA DE NAVEGAÇÃO
// =========================================================

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

        NavigationBarItem(

            selected =
                false,

            onClick = {
                navController.navigate(
                    "home"
                )
            },

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
                        11.sp
                )
            }
        )


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


        NavigationBarItem(

            selected =
                true,

            onClick = {},

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
                        11.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            },

            colors =
                NavigationBarItemDefaults.colors(

                    indicatorColor =
                        FigmaPrimaryBlue.copy(
                            alpha = 0.15f
                        ),

                    selectedTextColor =
                        FigmaPrimaryBlue
                )
        )
    }
}