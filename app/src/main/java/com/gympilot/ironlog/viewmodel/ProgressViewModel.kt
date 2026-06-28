package com.gympilot.ironlog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gympilot.ironlog.data.ExerciseProgressPoint
import com.gympilot.ironlog.data.HistoryLogItem
import com.gympilot.ironlog.data.IronLogDatabase
import com.gympilot.ironlog.data.IronLogRepository
import com.gympilot.ironlog.data.SessionWithVolume
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class ProgressUiState(
    val totalWorkouts: Int = 0,
    val currentStreak: Int = 0,
    val averageDurationSeconds: Long = 0,
    val totalVolume: Double = 0.0,
    val sessions: List<SessionWithVolume> = emptyList(),
    val exerciseNames: List<String> = emptyList(),
    val selectedExercise: String = "",
    val exerciseProgress: List<ExerciseProgressPoint> = emptyList(),
    val logsBySession: Map<Long, List<HistoryLogItem>> = emptyMap(),
    val weeklyFrequency: List<Int> = List(7) { 0 },
    val monthlyWorkouts: List<Int> = List(6) { 0 },
    val volumeOverTime: List<Double> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IronLogRepository(IronLogDatabase.getInstance(application).dao())
    private val selectedExercise = MutableStateFlow("")

    private val exerciseProgress = selectedExercise.flatMapLatest { name ->
        if (name.isBlank()) MutableStateFlow(emptyList()) else repository.progressForExercise(name)
    }

    val uiState: StateFlow<ProgressUiState> = combine(
        repository.sessions,
        repository.loggedExerciseNames,
        selectedExercise,
        exerciseProgress,
        repository.historyLogs
    ) { sessions, names, selected, progress, logs ->
        val chosen = selected.ifBlank { names.firstOrNull().orEmpty() }
        if (selected.isBlank() && chosen.isNotBlank()) {
            selectedExercise.value = chosen
        }
        ProgressUiState(
            totalWorkouts = sessions.size,
            currentStreak = currentStreak(sessions),
            averageDurationSeconds = sessions.map { it.durationSeconds }.averageOrZero().toLong(),
            totalVolume = sessions.sumOf { it.volume },
            sessions = sessions,
            logsBySession = logs.groupBy { it.sessionId },
            exerciseNames = names,
            selectedExercise = chosen,
            exerciseProgress = progress,
            weeklyFrequency = weeklyFrequency(sessions),
            monthlyWorkouts = monthlyWorkouts(sessions),
            volumeOverTime = sessions.sortedBy { it.finishedAt }.map { it.volume }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun selectExercise(name: String) {
        selectedExercise.value = name
    }

    private fun currentStreak(sessions: List<SessionWithVolume>): Int {
        val workoutDays = sessions.map { it.finishedAt.toLocalDate() }.toSet()
        if (workoutDays.isEmpty()) return 0
        var day = LocalDate.now()
        if (!workoutDays.contains(day)) {
            day = day.minusDays(1)
        }
        var streak = 0
        while (workoutDays.contains(day)) {
            streak += 1
            day = day.minusDays(1)
        }
        return streak
    }

    private fun weeklyFrequency(sessions: List<SessionWithVolume>): List<Int> {
        val today = LocalDate.now()
        return (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            sessions.count { it.finishedAt.toLocalDate() == day }
        }
    }

    private fun monthlyWorkouts(sessions: List<SessionWithVolume>): List<Int> {
        val current = YearMonth.now()
        return (5 downTo 0).map { offset ->
            val month = current.minusMonths(offset.toLong())
            sessions.count { YearMonth.from(it.finishedAt.toLocalDate()) == month }
        }
    }

    private fun List<Long>.averageOrZero(): Double =
        if (isEmpty()) 0.0 else average()

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}
