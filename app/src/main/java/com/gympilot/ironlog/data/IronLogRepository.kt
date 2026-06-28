package com.gympilot.ironlog.data

import kotlinx.coroutines.flow.Flow

class IronLogRepository(private val dao: IronLogDao) {
    val plans: Flow<List<WorkoutPlan>> = dao.observePlans()
    val planSummaries: Flow<List<WorkoutPlanSummary>> = dao.observePlanSummaries()
    val sessions: Flow<List<SessionWithVolume>> = dao.observeSessions()
    val loggedExerciseNames: Flow<List<String>> = dao.observeLoggedExerciseNames()
    val historyLogs: Flow<List<HistoryLogItem>> = dao.observeHistoryLogs()

    fun exercisesForPlan(planId: Long): Flow<List<ExerciseWithPrevious>> =
        dao.observeExercisesWithPrevious(planId)

    fun logsForSession(sessionId: Long): Flow<List<WorkoutLog>> =
        dao.observeLogsForSession(sessionId)

    fun progressForExercise(exerciseName: String): Flow<List<ExerciseProgressPoint>> =
        dao.observeExerciseProgress(exerciseName)

    suspend fun seedIfEmpty() {
        if (dao.countPlans() > 0) return
        val pushId = dao.insertPlan(WorkoutPlan(name = "Push Day", splitType = "Push"))
        listOf("Bench Press", "Incline Dumbbell Press", "Shoulder Press", "Triceps Pushdown")
            .forEachIndexed { index, name ->
                dao.insertExercise(Exercise(planId = pushId, name = name, sortOrder = index))
            }

        val pullId = dao.insertPlan(WorkoutPlan(name = "Pull Day", splitType = "Pull"))
        listOf("Deadlift", "Lat Pulldown", "Seated Row", "Biceps Curl")
            .forEachIndexed { index, name ->
                dao.insertExercise(Exercise(planId = pullId, name = name, sortOrder = index))
            }
    }

    suspend fun addPlan(name: String, splitType: String): Long =
        dao.insertPlan(WorkoutPlan(name = name.trim(), splitType = splitType))

    suspend fun addExercise(planId: Long, name: String, sortOrder: Int) {
        dao.insertExercise(Exercise(planId = planId, name = name.trim(), sortOrder = sortOrder))
    }

    suspend fun updateExercise(exercise: Exercise) {
        dao.updateExercise(exercise)
    }

    suspend fun deleteExercise(exerciseId: Long) {
        dao.deleteExercise(exerciseId)
    }

    suspend fun reorder(planId: Long, orderedIds: List<Long>) {
        val current = dao.getExercises(planId).associateBy { it.id }
        orderedIds.forEachIndexed { index, id ->
            current[id]?.let { dao.updateExercise(it.copy(sortOrder = index)) }
        }
    }

    suspend fun saveCompletedWorkout(
        plan: WorkoutPlan,
        startedAt: Long,
        finishedAt: Long,
        durationSeconds: Long,
        exercises: List<WorkoutExerciseUi>
    ) {
        val logs = exercises.map {
            WorkoutLog(
                sessionId = 0,
                exerciseName = it.name,
                weight = it.weight,
                notes = it.notes,
                completed = it.completed
            )
        }
        dao.saveSession(
            WorkoutSession(
                planId = plan.id,
                workoutName = plan.name,
                startedAt = startedAt,
                finishedAt = finishedAt,
                durationSeconds = durationSeconds
            ),
            logs
        )
    }
}

data class WorkoutExerciseUi(
    val id: Long,
    val planId: Long,
    val name: String,
    val weight: Double,
    val notes: String,
    val sortOrder: Int,
    val completed: Boolean,
    val previousWeight: Double?,
    val skipped: Boolean = false,
    val selectedSet: Int? = null
)
