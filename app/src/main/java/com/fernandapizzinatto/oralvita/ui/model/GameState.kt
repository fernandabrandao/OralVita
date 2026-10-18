package com.fernandapizzinatto.oralvita.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

object GameState {

    var points by mutableIntStateOf(0)

    var completedMissions by mutableIntStateOf(0)

    var totalMissions by mutableIntStateOf(4)

    var streak by mutableIntStateOf(7)

}