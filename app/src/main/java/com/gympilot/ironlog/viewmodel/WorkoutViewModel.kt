package com.gympilot.ironlog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gympilot.ironlog.data.Exercise
import com.gympilot.ironlog.data.IronLogDatabase
import com.gympilot.ironlog.data.IronLogRepository
import com.gympilot.ironlog.data.WorkoutExerciseUi
import com.gympilot.ironlog.data.WorkoutPlan
import com.gympilot.ironlog.data.WorkoutPlanSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WorkoutUiState(
    val plans: List<WorkoutPlan> = emptyList(),
    val planSummaries: List<WorkoutPlanSummary> = emptyList(),
    val selectedPlan: WorkoutPlan? = null,
    val exercises: List<WorkoutExerciseUi> = emptyList(),
    val elapsedSeconds: Long = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
)

private data class WorkoutContentState(
    val plans: List<WorkoutPlan>,
    val planSummaries: List<WorkoutPlanSummary>,
    val selectedPlan: WorkoutPlan?,
    val exercises: List<WorkoutExerciseUi>
)

private data class WorkoutMarks(
    val completedIds: Set<Long>,
    val skippedIds: Set<Long>,
    val selectedSets: Map<Long, Int>
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IronLogRepository(IronLogDatabase.getInstance(application).dao())
    private val selectedPlanId = MutableStateFlow<Long?>(null)
    private val elapsedSeconds = MutableStateFlow(0L)
    private val running = MutableStateFlow(false)
    private val paused = MutableStateFlow(false)
    private val completedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val skippedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val selectedSets = MutableStateFlow<Map<Long, Int>>(emptyMap())

    private val _reOrder = MutableStateFlow(false)
    val reOrder =_reOrder.asStateFlow()


    private var timerJob: Job? = null
    private var startedAt: Long = 0L
    private var activeStartedAt: Long = 0L
    private var accumulatedSeconds: Long = 0L
    private var timerStartedDay: LocalDate? = null

    private val plans = repository.plans.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val planSummaries = repository.planSummaries.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val exercises = selectedPlanId
        .flatMapLatest { planId ->
            if (planId == null) {
                MutableStateFlow(emptyList())
            } else {
                repository.exercisesForPlan(planId).map { rows ->
                    val completed = completedIds.value
                    val skipped = skippedIds.value
                    val sets = selectedSets.value
                    rows.map {
                        WorkoutExerciseUi(
                            id = it.id,
                            planId = it.planId,
                            name = it.name,
                            weight = it.weight,
                            notes = it.notes,
                            sortOrder = it.sortOrder,
                            completed = completed.contains(it.id),
                            previousWeight = it.previousWeight,
                            skipped = skipped.contains(it.id),
                            selectedSet = sets[it.id]
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val workoutMarks = combine(
        completedIds,
        skippedIds,
        selectedSets
    ) { completed, skipped, sets ->
        WorkoutMarks(completed, skipped, sets)
    }

    private val contentState = combine(
        plans,
        planSummaries,
        selectedPlanId,
        exercises,
        workoutMarks
    ) { planList, summaries, selectedId, exerciseList, marks ->
        val selected = planList.firstOrNull { it.id == selectedId } ?: planList.firstOrNull()
        val syncedExercises = exerciseList
            .map {
                it.copy(
                    completed = marks.completedIds.contains(it.id),
                    skipped = marks.skippedIds.contains(it.id),
                    selectedSet = marks.selectedSets[it.id]
                )
            }
            .sortedWith(
                compareBy<WorkoutExerciseUi> { it.completed || it.skipped }
                    .thenBy { it.sortOrder }
                    .thenBy { it.name }
            )
        WorkoutContentState(
            plans = planList,
            planSummaries = summaries,
            selectedPlan = selected,
            exercises = syncedExercises
        )
    }

    val uiState: StateFlow<WorkoutUiState> = combine(
        contentState,
        elapsedSeconds,
        running,
        paused
    ) { content, elapsed, isRunning, isPaused ->
        WorkoutUiState(
            plans = content.plans,
            planSummaries = content.planSummaries,
            selectedPlan = content.selectedPlan,
            exercises = content.exercises,
            elapsedSeconds = elapsed,
            isRunning = isRunning,
            isPaused = isPaused
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutUiState())

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
        viewModelScope.launch {
            plans.collect { planList ->
                if (selectedPlanId.value == null && planList.isNotEmpty()) {
                    selectedPlanId.value = planList.first().id
                }
            }
        }
    }

    fun selectPlan(planId: Long) {
        selectedPlanId.value = planId
        clearWorkoutMarks()
    }

    fun startTimer() {
        if (running.value) return
        startedAt = System.currentTimeMillis()
        activeStartedAt = startedAt
        accumulatedSeconds = 0L
        elapsedSeconds.value = 0L
        running.value = true
        paused.value = false
        timerStartedDay = LocalDate.now()
        launchTimer()
    }

    fun pauseTimer() {
        if (!running.value || paused.value) return
        accumulatedSeconds += (System.currentTimeMillis() - activeStartedAt) / 1_000
        paused.value = true
        timerJob?.cancel()
    }

    fun resumeTimer() {
        if (!running.value || !paused.value) return
        activeStartedAt = System.currentTimeMillis()
        paused.value = false
        launchTimer()
    }

    fun finishWorkout() {
        val state = uiState.value
        val plan = state.selectedPlan ?: return
        val finishedAt = System.currentTimeMillis()
        val duration = if (running.value && !paused.value) {
            accumulatedSeconds + (finishedAt - activeStartedAt) / 1_000
        } else {
            elapsedSeconds.value
        }.coerceAtLeast(1)

        viewModelScope.launch {
            repository.saveCompletedWorkout(
                plan = plan,
                startedAt = if (startedAt == 0L) finishedAt - duration * 1_000 else startedAt,
                finishedAt = finishedAt,
                durationSeconds = duration,
                exercises = state.exercises
            )
            resetTimer()
            clearWorkoutMarks()
        }
    }

    fun addPlan(name: String, splitType: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.addPlan(name, splitType)
            selectedPlanId.value = id
        }
    }

    fun addExercise(name: String) {
        val planId = selectedPlanId.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addExercise(planId, name, uiState.value.exercises.size)
        }
    }

    fun updateExercise(exercise: WorkoutExerciseUi, name: String, weight: Double, notes: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateExercise(
                Exercise(
                    id = exercise.id,
                    planId = exercise.planId,
                    name = name.trim(),
                    weight = weight,
                    notes = notes.trim(),
                    sortOrder = exercise.sortOrder
                )
            )
        }
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch {
            repository.deleteExercise(exerciseId)
            completedIds.value = completedIds.value - exerciseId
            skippedIds.value = skippedIds.value - exerciseId
            selectedSets.value = selectedSets.value - exerciseId
        }
    }

    fun markDone(exerciseId: Long) {
        completedIds.value = completedIds.value + exerciseId
        skippedIds.value = skippedIds.value - exerciseId
    }

    fun skipExercise(exerciseId: Long) {
        skippedIds.value = skippedIds.value + exerciseId
        completedIds.value = completedIds.value - exerciseId
    }

    fun selectSet(exerciseId: Long, setNumber: Int) {
        selectedSets.value = if (selectedSets.value[exerciseId] == setNumber) {
            selectedSets.value - exerciseId
        } else {
            selectedSets.value + (exerciseId to setNumber.coerceAtLeast(1))
        }
    }

    fun moveExercise(exerciseId: Long, direction: Int) {
        val state = uiState.value
        val index = state.exercises.indexOfFirst { it.id == exerciseId }
        val target = index + direction
        if (index !in state.exercises.indices || target !in state.exercises.indices) return
        val reordered = state.exercises.toMutableList().apply {
            add(target, removeAt(index))
        }
        viewModelScope.launch {
            repository.reorder(state.selectedPlan?.id ?: return@launch, reordered.map { it.id })
        }
    }

    private fun launchTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (running.value && !paused.value) {
                if (timerStartedDay != null && timerStartedDay != LocalDate.now()) {
                    finishWorkout()
                    break
                }
                elapsedSeconds.value =
                    accumulatedSeconds + (System.currentTimeMillis() - activeStartedAt) / 1_000
                delay(1_000)
            }
        }
    }

    private fun resetTimer() {
        timerJob?.cancel()
        startedAt = 0L
        activeStartedAt = 0L
        accumulatedSeconds = 0L
        elapsedSeconds.value = 0L
        running.value = false
        paused.value = false
        timerStartedDay = null
    }

    private fun clearWorkoutMarks() {
        completedIds.value = emptySet()
        skippedIds.value = emptySet()
        selectedSets.value = emptyMap()
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    fun reorderExercises() {
        _reOrder.value = reOrder.value.not()
    }
}
