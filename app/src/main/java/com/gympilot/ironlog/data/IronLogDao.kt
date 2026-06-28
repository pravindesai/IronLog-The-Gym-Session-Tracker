package com.gympilot.ironlog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IronLogDao {
    @Query("SELECT * FROM workout_plans ORDER BY createdAt ASC")
    fun observePlans(): Flow<List<WorkoutPlan>>

    @Query(
        """
        SELECT wp.id, wp.name, wp.splitType,
            (SELECT COUNT(*) FROM exercises e WHERE e.planId = wp.id) AS exerciseCount,
            (SELECT MAX(ws.finishedAt) FROM workout_sessions ws WHERE ws.planId = wp.id) AS lastUsedAt
        FROM workout_plans wp
        ORDER BY wp.createdAt ASC
        """
    )
    fun observePlanSummaries(): Flow<List<WorkoutPlanSummary>>

    @Query(
        """
        SELECT e.id, e.planId, e.name, e.weight, e.notes, e.sortOrder,
            (
                SELECT wl.weight
                FROM workout_logs wl
                INNER JOIN workout_sessions ws ON ws.id = wl.sessionId
                WHERE wl.exerciseName = e.name AND wl.completed = 1
                ORDER BY ws.finishedAt DESC
                LIMIT 1
            ) AS previousWeight
        FROM exercises e
        WHERE e.planId = :planId
        ORDER BY e.sortOrder ASC, e.name ASC
        """
    )
    fun observeExercisesWithPrevious(planId: Long): Flow<List<ExerciseWithPrevious>>

    @Query("SELECT * FROM exercises WHERE planId = :planId ORDER BY sortOrder ASC, name ASC")
    suspend fun getExercises(planId: Long): List<Exercise>

    @Query("SELECT COUNT(*) FROM workout_plans")
    suspend fun countPlans(): Int

    @Insert
    suspend fun insertPlan(plan: WorkoutPlan): Long

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Insert
    suspend fun insertExercise(exercise: Exercise): Long

    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: Long)

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertLogs(logs: List<WorkoutLog>)

    @Transaction
    suspend fun saveSession(session: WorkoutSession, logs: List<WorkoutLog>) {
        val sessionId = insertSession(session)
        insertLogs(logs.map { it.copy(sessionId = sessionId) })
    }

    @Query(
        """
        SELECT ws.id, ws.planId, ws.workoutName, ws.startedAt, ws.finishedAt, ws.durationSeconds,
            COALESCE(SUM(CASE WHEN wl.completed = 1 THEN wl.weight ELSE 0 END), 0) AS volume
        FROM workout_sessions ws
        LEFT JOIN workout_logs wl ON wl.sessionId = ws.id
        GROUP BY ws.id
        ORDER BY ws.finishedAt DESC
        """
    )
    fun observeSessions(): Flow<List<SessionWithVolume>>

    @Query("SELECT * FROM workout_logs WHERE sessionId = :sessionId ORDER BY id ASC")
    fun observeLogsForSession(sessionId: Long): Flow<List<WorkoutLog>>

    @Query(
        """
        SELECT sessionId, exerciseName, weight, notes, completed
        FROM workout_logs
        ORDER BY id ASC
        """
    )
    fun observeHistoryLogs(): Flow<List<HistoryLogItem>>

    @Query("SELECT DISTINCT exerciseName FROM workout_logs ORDER BY exerciseName ASC")
    fun observeLoggedExerciseNames(): Flow<List<String>>

    @Query(
        """
        SELECT ws.finishedAt AS date, wl.weight AS weight
        FROM workout_logs wl
        INNER JOIN workout_sessions ws ON ws.id = wl.sessionId
        WHERE wl.exerciseName = :exerciseName AND wl.completed = 1
        ORDER BY ws.finishedAt ASC
        """
    )
    fun observeExerciseProgress(exerciseName: String): Flow<List<ExerciseProgressPoint>>
}
