package com.fernandapizzinatto.oralvita.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.fernandapizzinatto.oralvita.R
import androidx.compose.ui.layout.ContentScale

// ============================================================
// MODELO VISUAL
// ============================================================

data class FigmaMissionVisual(
    val title: String,
    val description: String,
    val points: Int,
    val icon: ImageVector,
    val themeColor: Color,
    val lightBgColor: Color
)

// ============================================================
// CORES ORIGINAIS
// ============================================================

private val FigmaBackground = Color(0xFFF0F6FC)
private val FigmaWhite = Color(0xFFFFFFFF)
private val FigmaTextBlack = Color(0xFF1E293B)
private val FigmaTextGrey = Color(0xFF64748B)
private val FigmaTextMuted = Color(0xFF94A3B8)
private val FigmaPrimaryBlue = Color(0xFF5294F7)
private val FigmaLightBlue = Color(0xFFE2EDFE)
private val FigmaBorder = Color(0xFFE2E8F0)

// ============================================================
// CONTROLE DE DATA
// ============================================================

private fun obterDataHoje(): String {

    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(Date())
}

// ============================================================
// OBTER DIA ANTERIOR
// ============================================================

private fun obterDataAnterior(
    data: String
): String {

    val formatter = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    )

    val calendar = Calendar.getInstance()

    calendar.time =
        formatter.parse(data) ?: Date()

    calendar.add(
        Calendar.DAY_OF_MONTH,
        -1
    )

    return formatter.format(
        calendar.time
    )
}

// ============================================================
// CALCULAR STREAK
// ============================================================

private fun calcularStreak(
    historico: Map<*, *>,
    dataInicial: String
): Int {

    var streak = 0

    var dataAtual =
        dataInicial

    while (true) {

        val dadosDoDia =
            historico[dataAtual] as? Map<*, *>

        val missoesConcluidas =
            dadosDoDia
                ?.get("missoesConcluidas")
                ?.toString()
                ?.toIntOrNull()
                ?: 0

        if (missoesConcluidas > 0) {

            streak++

            dataAtual =
                obterDataAnterior(
                    dataAtual
                )

        } else {

            break
        }
    }

    return streak
}

// ============================================================
// TELA DE DESAFIOS
// ============================================================

@Composable
fun ChallengesScreen(
    navController: NavController
) {

    val auth =
        FirebaseAuth.getInstance()

    val db =
        FirebaseFirestore.getInstance()

    // --------------------------------------------------------
    // MISSÕES ORIGINAIS
    // --------------------------------------------------------

    val missionsVisual =
        listOf(

            FigmaMissionVisual(
                title = "Escovar de manhã",
                description =
                    "Escove os dentes logo ao acordar para começar o dia fresquinho!",
                points = 20,
                icon = Icons.Default.WbSunny,
                themeColor =
                    Color(0xFFEAB308),
                lightBgColor =
                    Color(0xFFFEF9C3)
            ),

            FigmaMissionVisual(
                title = "2 minutos de escovação",
                description =
                    "Escove por pelo menos 2 minutos para remover toda a placa bacteriana.",
                points = 20,
                icon = Icons.Default.AccessTime,
                themeColor =
                    Color(0xFF38BDF8),
                lightBgColor =
                    Color(0xFFE0F2FE)
            ),

            FigmaMissionVisual(
                title = "Usar fio dental",
                description =
                    "O fio dental alcança onde a escova não chega. Um passo importante!",
                points = 20,
                icon = Icons.Default.Air,
                themeColor =
                    Color(0xFF34D399),
                lightBgColor =
                    Color(0xFFD1FAE5)
            ),

            FigmaMissionVisual(
                title = "Escovar antes de dormir",
                description =
                    "Proteja seus dentes durante a noite com uma boa escovação noturna.",
                points = 20,
                icon = Icons.Default.Nightlight,
                themeColor =
                    Color(0xFFA78BFA),
                lightBgColor =
                    Color(0xFFEDE9FE)
            )
        )

    // --------------------------------------------------------
    // ESTADOS
    // --------------------------------------------------------

    val checkedStates =
        remember {
            mutableStateListOf(
                false,
                false,
                false,
                false
            )
        }

    val lockedStates =
        remember {
            mutableStateListOf(
                false,
                false,
                false,
                false
            )
        }

    var selectedTab by remember {
        mutableStateOf("Diárias")
    }

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    var mensagem by remember {
        mutableStateOf<String?>(null)
    }

    var mostrarConfirmacao by remember {
        mutableStateOf(false)
    }

    var missaoSelecionada by remember {
        mutableStateOf<Int?>(null)
    }

    // --------------------------------------------------------
    // MISSÃO EM EXECUÇÃO
    //
    // 1 = 2 minutos de escovação
    // 2 = fio dental
    // 3 = escovar de manhã
    // 4 = escovar antes de dormir
    // --------------------------------------------------------

    var missaoEmExecucao by remember {
        mutableStateOf<Int?>(null)
    }

    // --------------------------------------------------------
    // MISSÃO DO TIMER
    //
    // 1 = 2 minutos
    // 3 = escovar antes de dormir
    // --------------------------------------------------------

    var missaoTimer by remember {
        mutableStateOf<Int?>(null)
    }

    var segundosRestantes by remember {
        mutableIntStateOf(120)
    }

    var etapaFioDental by remember {
        mutableIntStateOf(1)
    }

    var etapaManha by remember {
        mutableIntStateOf(1)
    }

    var mostrarParabens by remember {
        mutableStateOf(false)
    }

    // --------------------------------------------------------
    // DATA ATUAL
    // --------------------------------------------------------

    val dataHoje =
        remember {
            obterDataHoje()
        }

    // ========================================================
    // REGISTRAR CONCLUSÃO NO FIREBASE
    // ========================================================

    fun registrarConclusaoNoFirebase(
        indiceDaMissao: Int
    ) {

        auth.currentUser?.let { user ->

            val documento =
                db.collection("usuarios")
                    .document(user.uid)

            documento.get()
                .addOnSuccessListener { document ->

                    // ----------------------------------------
                    // HISTÓRICO EXISTENTE
                    // ----------------------------------------

                    val historicoExistente =
                        document.get(
                            "historicoDiario"
                        ) as? Map<*, *>

                    val historico =
                        mutableMapOf<String, Any>()

                    historicoExistente?.forEach {
                            (chave, valor) ->

                        if (
                            chave != null &&
                            valor != null
                        ) {

                            historico[
                                chave.toString()
                            ] = valor
                        }
                    }

                    // ----------------------------------------
                    // MISSÕES DO DIA
                    // ----------------------------------------

                    val missoesDoDia =
                        checkedStates.toList()

                    val quantidadeConcluida =
                        missoesDoDia.count {
                            it
                        }

                    val pontosDoDia =
                        quantidadeConcluida * 20

                    // ----------------------------------------
                    // DADOS DO DIA
                    // ----------------------------------------

                    val dadosDoDia =
                        mapOf(

                            "missoesConcluidas" to
                                    quantidadeConcluida,

                            "pontos" to
                                    pontosDoDia,

                            "escovarManha" to
                                    missoesDoDia.getOrElse(
                                        0
                                    ) {
                                        false
                                    },

                            "doisMinutos" to
                                    missoesDoDia.getOrElse(
                                        1
                                    ) {
                                        false
                                    },

                            "fioDental" to
                                    missoesDoDia.getOrElse(
                                        2
                                    ) {
                                        false
                                    },

                            "escovarNoite" to
                                    missoesDoDia.getOrElse(
                                        3
                                    ) {
                                        false
                                    }
                        )

                    // ----------------------------------------
                    // SALVAR DIA ATUAL NO HISTÓRICO
                    // ----------------------------------------

                    historico[dataHoje] =
                        dadosDoDia

                    // ----------------------------------------
                    // CALCULAR STREAK
                    // ----------------------------------------

                    val novoStreak =
                        calcularStreak(
                            historico =
                                historico,
                            dataInicial =
                                dataHoje
                        )

                    GameState.streak =
                        novoStreak

                    // ----------------------------------------
                    // SALVAR NO FIREBASE
                    // ----------------------------------------

                    documento.update(

                        mapOf(

                            "pontos" to
                                    GameState.points,

                            "missoesConcluidas" to
                                    GameState.completedMissions,

                            "missoes" to
                                    checkedStates.toList(),

                            "missoesBloqueadas" to
                                    lockedStates.toList(),

                            "streak" to
                                    novoStreak,

                            "dataMissoes" to
                                    dataHoje,

                            "historicoDiario" to
                                    historico
                        )
                    )
                }
        }
    }

    // ========================================================
    // CARREGAR DADOS DO FIREBASE
    // ========================================================

    LaunchedEffect(Unit) {

        auth.currentUser?.let { user ->

            db.collection("usuarios")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->

                    val dataSalva =
                        document.getString(
                            "dataMissoes"
                        )

                    // ----------------------------------------
                    // VERIFICAR NOVO DIA
                    // ----------------------------------------

                    val ehNovoDia =
                        dataSalva != null &&
                                dataSalva != dataHoje

                    if (ehNovoDia) {

                        // ------------------------------------
                        // ZERAR MISSÕES DO NOVO DIA
                        // ------------------------------------

                        repeat(
                            checkedStates.size
                        ) { index ->

                            checkedStates[index] =
                                false

                            lockedStates[index] =
                                false
                        }

                        GameState.completedMissions =
                            0

                        // ------------------------------------
                        // MANTER PONTOS
                        // ------------------------------------

                        val pontos =
                            document.getLong(
                                "pontos"
                            )?.toInt()

                        pontos?.let {
                            GameState.points = it
                        }

                        // ------------------------------------
                        // MANTER STREAK
                        // ------------------------------------

                        val streak =
                            document.getLong(
                                "streak"
                            )?.toInt()

                        GameState.streak =
                            streak ?: 0

                        // ------------------------------------
                        // SALVAR NOVO DIA
                        // ------------------------------------

                        db.collection("usuarios")
                            .document(user.uid)
                            .update(

                                mapOf(

                                    "dataMissoes" to
                                            dataHoje,

                                    "missoesConcluidas" to
                                            0,

                                    "missoes" to
                                            listOf(
                                                false,
                                                false,
                                                false,
                                                false
                                            ),

                                    "missoesBloqueadas" to
                                            listOf(
                                                false,
                                                false,
                                                false,
                                                false
                                            )
                                )
                            )

                    } else {

                        // ------------------------------------
                        // MESMO DIA
                        // ------------------------------------

                        val missoes =
                            document.get(
                                "missoes"
                            ) as? List<*>

                        val missoesBloqueadas =
                            document.get(
                                "missoesBloqueadas"
                            ) as? List<*>

                        missoesBloqueadas?.forEachIndexed {
                                index,
                                bloqueada ->

                            if (
                                index <
                                lockedStates.size
                            ) {

                                lockedStates[index] =
                                    bloqueada
                                            as? Boolean
                                        ?: false
                            }
                        }

                        missoes?.forEachIndexed {
                                index,
                                concluida ->

                            if (
                                index <
                                checkedStates.size
                            ) {

                                checkedStates[index] =
                                    concluida
                                            as? Boolean
                                        ?: false
                            }
                        }

                        // ------------------------------------
                        // PONTOS
                        // ------------------------------------

                        val pontos =
                            document.getLong(
                                "pontos"
                            )?.toInt()

                        pontos?.let {
                            GameState.points = it
                        }

                        // ------------------------------------
                        // STREAK
                        // ------------------------------------

                        val streak =
                            document.getLong(
                                "streak"
                            )?.toInt()

                        GameState.streak =
                            streak ?: 0

                        // ------------------------------------
                        // MISSÕES CONCLUÍDAS
                        // ------------------------------------

                        GameState.completedMissions =
                            checkedStates.count {
                                it
                            }
                    }

                    // ----------------------------------------
                    // PRIMEIRO ACESSO
                    // ----------------------------------------

                    if (dataSalva == null) {

                        db.collection("usuarios")
                            .document(user.uid)
                            .update(
                                "dataMissoes",
                                dataHoje
                            )
                    }
                }
        }
    }

    // ========================================================
    // TIMER DE 2 MINUTOS
    // ========================================================

    LaunchedEffect(missaoTimer) {

        if (missaoTimer != null) {

            segundosRestantes = 120

            while (
                segundosRestantes > 0 &&
                missaoTimer != null
            ) {

                delay(1000)

                if (missaoTimer != null) {
                    segundosRestantes--
                }
            }

            // ------------------------------------------------
            // TIMER TERMINOU
            // ------------------------------------------------

            if (
                segundosRestantes == 0 &&
                missaoTimer != null
            ) {

                val indiceDaMissao =
                    missaoTimer

                if (
                    indiceDaMissao != null
                ) {

                    // ----------------------------------------
                    // CONCLUIR MISSÃO CORRETA
                    // ----------------------------------------

                    checkedStates[
                        indiceDaMissao
                    ] = true

                    lockedStates[
                        indiceDaMissao
                    ] = true

                    GameState.completedMissions =
                        checkedStates.count {
                            it
                        }

                    GameState.points += 20

                    // ----------------------------------------
                    // REGISTRAR HISTÓRICO
                    // ----------------------------------------

                    registrarConclusaoNoFirebase(
                        indiceDaMissao =
                            indiceDaMissao
                    )

                    // ----------------------------------------
                    // ENCERRAR TIMER
                    // ----------------------------------------

                    missaoTimer = null

                    missaoEmExecucao = null

                    // ----------------------------------------
                    // PARABÉNS
                    // ----------------------------------------

                    mostrarParabens = true
                }
            }
        }
    }

    // ========================================================
    // INTERFACE
    // ========================================================

    Scaffold(

        snackbarHost = {

            SnackbarHost(
                hostState =
                    snackbarHostState
            )
        },

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
        ) {

            // =================================================
            // MISSÕES INTERATIVAS
            // =================================================

            when (missaoEmExecucao) {

                // ------------------------------------------------
                // 2 MINUTOS
                // ------------------------------------------------

                1 -> {
                    EscovacaoTimerScreen(
                        segundosRestantes = segundosRestantes,
                        exibirGifs = missaoTimer == 1,
                        onCancelar = {
                            missaoTimer = null
                            missaoEmExecucao = null
                        }
                    )
                }

                // ------------------------------------------------
                // FIO DENTAL
                // ------------------------------------------------

                2 -> {

                    FioDentalScreen(

                        etapaAtual =
                            etapaFioDental,

                        onProximaEtapa = {

                            if (
                                etapaFioDental < 3
                            ) {

                                etapaFioDental++

                            } else {

                                checkedStates[2] =
                                    true

                                lockedStates[2] =
                                    true

                                GameState.completedMissions =
                                    checkedStates.count {
                                        it
                                    }

                                GameState.points += 20

                                registrarConclusaoNoFirebase(
                                    indiceDaMissao = 2
                                )

                                missaoEmExecucao =
                                    null

                                mostrarParabens =
                                    true
                            }
                        }
                    )
                }

                // ------------------------------------------------
                // ESCOVAR DE MANHÃ
                // ------------------------------------------------

                3 -> {

                    EscovarDeManhaScreen(

                        etapaAtual =
                            etapaManha,

                        onProximaEtapa = {

                            if (
                                etapaManha < 3
                            ) {

                                etapaManha++

                            } else {

                                checkedStates[0] =
                                    true

                                lockedStates[0] =
                                    true

                                GameState.completedMissions =
                                    checkedStates.count {
                                        it
                                    }

                                GameState.points += 20

                                registrarConclusaoNoFirebase(
                                    indiceDaMissao = 0
                                )

                                missaoEmExecucao =
                                    null

                                mostrarParabens =
                                    true
                            }
                        }
                    )
                }

                // ------------------------------------------------
                // ESCOVAR ANTES DE DORMIR
                // ------------------------------------------------


                4 -> {
                    EscovarAntesDeDormirScreen(
                        onIniciar = {
                            checkedStates[3] = true
                            lockedStates[3] = true

                            GameState.completedMissions =
                                checkedStates.count { it }

                            GameState.points += 20

                            registrarConclusaoNoFirebase(
                                indiceDaMissao = 3
                            )

                            missaoEmExecucao = null
                            missaoTimer = null
                            mostrarParabens = true
                        },
                        onCancelar = {
                            missaoTimer = null
                            missaoEmExecucao = null
                        }
                    )
                }


                // =================================================
                // TELA ORIGINAL
                // =================================================

                else -> {

                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(

                            modifier = Modifier
                                .size(40.dp)
                                .clip(
                                    RoundedCornerShape(
                                        20.dp
                                    )
                                )
                                .background(
                                    FigmaWhite
                                )
                                .clickable {

                                    navController
                                        .popBackStack()
                                },

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(

                                imageVector =
                                    Icons.AutoMirrored
                                        .Filled.ArrowBack,

                                contentDescription =
                                    "Voltar",

                                tint =
                                    FigmaTextBlack
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.width(12.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(

                                text =
                                    "Desafios",

                                fontSize =
                                    22.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    FigmaTextBlack
                            )

                            Text(

                                text =
                                    "${checkedStates.count { it }} de ${missionsVisual.size} concluídas",

                                fontSize =
                                    12.sp,

                                color =
                                    FigmaTextGrey
                            )
                        }

                        Text(
                            text = "🪥",
                            fontSize = 28.sp
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    // ------------------------------------------------
                    // ABAS
                    // ------------------------------------------------

                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp
                            ),

                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        FigmaTabChip(

                            title =
                                "Diárias",

                            icon =
                                Icons.Default.WbSunny,

                            isSelected =
                                selectedTab ==
                                        "Diárias",

                            onClick = {
                                selectedTab =
                                    "Diárias"
                            },

                            modifier =
                                Modifier.weight(1f)
                        )

                        FigmaTabChip(

                            title =
                                "Semanais",

                            icon =
                                Icons.Default.CalendarToday,

                            isSelected =
                                selectedTab ==
                                        "Semanais",

                            onClick = {
                                selectedTab =
                                    "Semanais"
                            },

                            modifier =
                                Modifier.weight(1f)
                        )

                        FigmaTabChip(

                            title =
                                "Mensais",

                            icon =
                                Icons.Default.CalendarToday,

                            isSelected =
                                selectedTab ==
                                        "Mensais",

                            onClick = {
                                selectedTab =
                                    "Mensais"
                            },

                            modifier =
                                Modifier.weight(1f)
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    HorizontalDivider(
                        color =
                            FigmaBorder
                    )

                    // ------------------------------------------------
                    // LISTA
                    // ------------------------------------------------

                    LazyColumn(

                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 16.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {

                        itemsIndexed(
                            missionsVisual
                        ) { index, visual ->

                            val isChecked =
                                checkedStates[index]

                            val isLocked =
                                lockedStates[index]

                            FigmaChallengeCard(

                                visual =
                                    visual,

                                isChecked =
                                    isChecked,

                                isLocked =
                                    isLocked,

                                onButtonClick = {

                                    if (!isLocked) {

                                        missaoSelecionada =
                                            index

                                        mostrarConfirmacao =
                                            true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ============================================================
    // CONFIRMAÇÃO
    // ============================================================

    if (mostrarConfirmacao) {

        val index =
            missaoSelecionada ?: return

        AlertDialog(

            onDismissRequest = {

                mostrarConfirmacao =
                    false
            },

            title = {

                Text(

                    if (index == 3)
                        "🌙 Hora de dormir!"
                    else
                        "🦷 Confirmar missão"
                )
            },

            text = {

                Text(

                    if (index == 3) {

                        "Está pronto para escovar os dentes antes de dormir?"

                    } else {

                        "Deseja iniciar essa missão?"
                    }
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        mostrarConfirmacao =
                            false

                        when (index) {

                            // ------------------------------------
                            // MANHÃ
                            // ------------------------------------

                            0 -> {

                                etapaManha = 1

                                missaoEmExecucao =
                                    3
                            }

                            // ------------------------------------
                            // 2 MINUTOS
                            // ------------------------------------

                            1 -> {

                                missaoTimer = 1

                                missaoEmExecucao =
                                    1
                            }

                            // ------------------------------------
                            // FIO DENTAL
                            // ------------------------------------

                            2 -> {

                                etapaFioDental = 1

                                missaoEmExecucao =
                                    2
                            }

                            // ------------------------------------
                            // NOITE
                            // ------------------------------------

                            3 -> {

                                missaoEmExecucao =
                                    4
                            }
                        }
                    }
                ) {

                    Text("Começar")
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        mostrarConfirmacao =
                            false
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }

    // ============================================================
    // PARABÉNS
    // ============================================================

    if (mostrarParabens) {

        AlertDialog(

            onDismissRequest = {

                mostrarParabens =
                    false
            },

            title = {

                Text(
                    text =
                        "🎉 Parabéns!"
                )
            },

            text = {

                Text(
                    text =
                        "Você concluiu a missão!\n\n+20 pontos"
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        mostrarParabens =
                            false
                    }
                ) {

                    Text("Continuar")
                }
            }
        )
    }
}

// ============================================================
// TIMER DE ESCOVAÇÃO
// ============================================================


@Composable
private fun EscovacaoTimerScreen(
    segundosRestantes: Int,
    onCancelar: () -> Unit,
    exibirGifs: Boolean = true
) {
    val minutos = segundosRestantes / 60
    val segundos = segundosRestantes % 60

    // O cronômetro vai de 120 até 0.
    val tempoDecorrido = (120 - segundosRestantes)
        .coerceIn(0, 120)

    // Progresso crescente: 0% no início e 100% no final.
    val progresso = tempoDecorrido / 120f

    val gifsEscovacao = listOf(
        R.drawable.escovacao_1,
        R.drawable.escovacao_2,
        R.drawable.escovacao_3,
        R.drawable.escovacao_4,
        R.drawable.escovacao_5,
        R.drawable.escovacao_6,
        R.drawable.escovacao_7
    )

    // 7 GIFs distribuídos ao longo dos 120 segundos.
    val indiceGif = (tempoDecorrido * 7 / 120)
        .coerceIn(0, 6)

    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FigmaBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(text = "🪥", fontSize = 60.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (exibirGifs) {
                "Hora de escovar!"
            } else {
                "Escovação noturna"
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = FigmaTextBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Escove os dentes durante 2 minutos.",
            fontSize = 14.sp,
            color = FigmaTextGrey
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutos,
                segundos
            ),
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = FigmaPrimaryBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        LinearProgressIndicator(
            progress = { progresso },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Continue escovando... 🦷✨",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = FigmaTextGrey
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (exibirGifs) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = gifsEscovacao[indiceGif],
                    imageLoader = imageLoader
                ),
                contentDescription =
                    "Demonstração do movimento ${indiceGif + 1} da escovação",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌙 🦷",
                    fontSize = 64.sp
                )
            }
        }

        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Sair")
        }
    }
}


// ============================================================
// ESCOVAR ANTES DE DORMIR
// ============================================================


@Composable
private fun EscovarAntesDeDormirScreen(
    onIniciar: () -> Unit,
    onCancelar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FigmaBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "🌙",
            fontSize = 60.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Escovação noturna",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = FigmaTextBlack
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Vamos cuidar dos seus dentes antes de dormir!",
            fontSize = 15.sp,
            color = FigmaTextGrey,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        EscovacaoGifsScreen(
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onIniciar,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA78BFA)
            )
        ) {
            Text(
                text = "Concluir missão",
                fontWeight = FontWeight.Bold,
                color = FigmaWhite
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Sair")
        }
    }
}

// ============================================================
// FIO DENTAL
// ============================================================

@Composable
private fun FioDentalGifsScreen(
    modifier: Modifier = Modifier
) {
    val gifsFioDental = listOf(
        R.drawable.fiodental_1,
        R.drawable.fiodental_2,
        R.drawable.fiodental_3,
        R.drawable.fiodental_4
    )

    var indiceGif by remember {
        mutableIntStateOf(0)
    }

    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            indiceGif =
                (indiceGif + 1) % gifsFioDental.size
        }
    }

    Image(
        painter = rememberAsyncImagePainter(
            model = gifsFioDental[indiceGif],
            imageLoader = imageLoader
        ),
        contentDescription =
            "Demonstração de como usar o fio dental",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun FioDentalScreen(
    etapaAtual: Int,
    onProximaEtapa: () -> Unit
) {

    val titulo =
        when (etapaAtual) {

            1 ->
                "Pegue o fio dental 🧵"

            2 ->
                "Passe entre os dentes 🦷"

            else ->
                "Pronto! ✨"
        }

    val descricao =
        when (etapaAtual) {

            1 ->
                "Separe um pedaço de fio dental para começar."

            2 ->
                "Passe o fio delicadamente entre todos os dentes."

            else ->
                "Você terminou sua missão de fio dental!"
        }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(
                FigmaBackground
            )
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        Text(
            text = "🧵",
            fontSize = 60.sp
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(

            text =
                titulo,

            fontSize =
                24.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                FigmaTextBlack
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Text(

            text =
                descricao,

            fontSize =
                15.sp,

            color =
                FigmaTextGrey
        )

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        LinearProgressIndicator(

            progress =
                etapaAtual / 3f,

            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(
                    RoundedCornerShape(5.dp)
                )
        )

        FioDentalGifsScreen(
            modifier = Modifier.weight(1f)
        )

        Button(

            onClick =
                onProximaEtapa,

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),

            shape =
                RoundedCornerShape(16.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF34D399)
                )
        ) {

            Text(

                text =
                    if (etapaAtual < 3)
                        "Próximo"
                    else
                        "Concluir missão",

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

// ============================================================
// ESCOVAR DE MANHÃ
// ============================================================

@Composable
private fun EscovacaoGifsScreen(
    modifier: Modifier = Modifier
) {
    val gifsEscovacao = listOf(
        R.drawable.escovacao_1,
        R.drawable.escovacao_2,
        R.drawable.escovacao_3,
        R.drawable.escovacao_4,
        R.drawable.escovacao_5,
        R.drawable.escovacao_6,
        R.drawable.escovacao_7
    )

    var indiceGif by remember {
        mutableIntStateOf(0)
    }

    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            indiceGif =
                (indiceGif + 1) % gifsEscovacao.size
        }
    }

    Image(
        painter = rememberAsyncImagePainter(
            model = gifsEscovacao[indiceGif],
            imageLoader = imageLoader
        ),
        contentDescription =
            "Demonstração dos movimentos de escovação",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}


@Composable
private fun EscovarDeManhaScreen(
    etapaAtual: Int,
    onProximaEtapa: () -> Unit
) {

    val titulo =
        when (etapaAtual) {

            1 ->
                "Bom dia! ☀️"

            2 ->
                "Vamos escovar! 🪥"

            else ->
                "Muito bem! ✨"
        }

    val descricao =
        when (etapaAtual) {

            1 ->
                "Comece o dia cuidando dos seus dentes."

            2 ->
                "Escove todos os dentes com movimentos suaves."

            else ->
                "Você completou sua escovação da manhã!"
        }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(
                FigmaBackground
            )
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        Text(
            text = "☀️",
            fontSize = 60.sp
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(

            text =
                titulo,

            fontSize =
                24.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                FigmaTextBlack
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Text(

            text =
                descricao,

            fontSize =
                15.sp,

            color =
                FigmaTextGrey
        )

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )

        LinearProgressIndicator(

            progress =
                etapaAtual / 3f,

            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(
                    RoundedCornerShape(5.dp)
                )
        )

        EscovacaoGifsScreen(
            modifier = Modifier.weight(1f)
        )

        Button(

            onClick =
                onProximaEtapa,

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),

            shape =
                RoundedCornerShape(16.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFFEAB308)
                )
        ) {

            Text(

                text =
                    if (etapaAtual < 3)
                        "Próximo"
                    else
                        "Concluir missão",

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

// ============================================================
// CHIP DAS ABAS
// ============================================================

@Composable
private fun FigmaTabChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(

        modifier = modifier
            .height(40.dp)
            .clip(
                RoundedCornerShape(20.dp)
            )
            .background(

                if (isSelected)
                    FigmaPrimaryBlue
                else
                    Color(0xFFF1F5F9)
            )
            .clickable {
                onClick()
            },

        contentAlignment =
            Alignment.Center
    ) {

        Row(

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.Center
        ) {

            Icon(

                imageVector =
                    icon,

                contentDescription =
                    null,

                tint =
                    if (isSelected)
                        FigmaWhite
                    else
                        FigmaTextGrey,

                modifier =
                    Modifier.size(16.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(6.dp)
            )

            Text(

                text =
                    title,

                fontSize =
                    12.sp,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    if (isSelected)
                        FigmaWhite
                    else
                        FigmaTextGrey
            )
        }
    }
}

// ============================================================
// CARD ORIGINAL
// ============================================================

@Composable
private fun FigmaChallengeCard(
    visual: FigmaMissionVisual,
    isChecked: Boolean,
    isLocked: Boolean,
    onButtonClick: () -> Unit
) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                FigmaBorder,
                RoundedCornerShape(20.dp)
            ),

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
                    Alignment.Top
            ) {

                Row(

                    modifier =
                        Modifier.weight(1f),

                    verticalAlignment =
                        Alignment.Top
                ) {

                    Box(

                        modifier = Modifier
                            .size(52.dp)
                            .clip(
                                RoundedCornerShape(
                                    16.dp
                                )
                            )
                            .background(
                                visual.lightBgColor
                            ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(

                            imageVector =
                                visual.icon,

                            contentDescription =
                                null,

                            tint =
                                visual.themeColor,

                            modifier =
                                Modifier.size(26.dp)
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(14.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(

                            text =
                                visual.title,

                            fontSize =
                                16.sp,

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
                                visual.description,

                            fontSize =
                                12.sp,

                            color =
                                FigmaTextGrey,

                            lineHeight =
                                16.sp
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Box(

                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        )
                        .background(
                            visual.lightBgColor
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        )
                ) {

                    Text(

                        text =
                            "+${visual.points}pts",

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            visual.themeColor
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Button(

                onClick =
                    onButtonClick,

                enabled =
                    !isLocked,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),

                colors =
                    ButtonDefaults.buttonColors(

                        containerColor =
                            if (isChecked)
                                FigmaTextMuted
                            else
                                visual.themeColor,

                        disabledContainerColor =
                            FigmaTextMuted
                    ),

                shape =
                    RoundedCornerShape(16.dp)
            ) {

                Text(

                    text =
                        if (isChecked)
                            "Missão Concluída ✓"
                        else
                            "Concluir Missão",

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        FigmaWhite
                )
            }
        }
    }
}

// ============================================================
// NAVEGAÇÃO INFERIOR ORIGINAL
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
                    fontSize = 18.sp
                )
            },

            label = {

                Text(
                    "Início",
                    fontSize = 11.sp
                )
            }
        )

        NavigationBarItem(

            selected =
                true,

            onClick = {},

            icon = {

                Text(
                    "✨",
                    fontSize = 18.sp
                )
            },

            label = {

                Text(
                    "Desafios",
                    fontSize = 11.sp,
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
                    fontSize = 18.sp
                )
            },

            label = {

                Text(
                    "Progresso",
                    fontSize = 11.sp
                )
            }
        )
    }
}