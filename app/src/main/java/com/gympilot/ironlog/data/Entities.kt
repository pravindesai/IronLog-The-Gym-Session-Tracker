package com.gympilot.ironlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val splitType: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val name: String,
    val weight: Double = 0.0,
    val notes: String = "",
    val sortOrder: Int = 0
)

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("planId")]
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long?,
    val workoutName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val durationSeconds: Long
)

@Entity(
    tableName = "workout_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val weight: Double,
    val notes: String = "",
    val completed: Boolean
)

data class ExerciseWithPrevious(
    val id: Long,
    val planId: Long,
    val name: String,
    val weight: Double,
    val notes: String,
    val sortOrder: Int,
    val previousWeight: Double?
)

data class SessionWithVolume(
    val id: Long,
    val planId: Long?,
    val workoutName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val durationSeconds: Long,
    val volume: Double
)

data class ExerciseProgressPoint(
    val date: Long,
    val weight: Double
)

data class HistoryLogItem(
    val sessionId: Long,
    val exerciseName: String,
    val weight: Double,
    val notes: String,
    val completed: Boolean
)

data class WorkoutPlanSummary(
    val id: Long,
    val name: String,
    val splitType: String,
    val exerciseCount: Int,
    val lastUsedAt: Long?
)
