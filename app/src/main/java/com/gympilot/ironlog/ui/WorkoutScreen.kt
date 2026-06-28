package com.gympilot.ironlog.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympilot.ironlog.data.WorkoutExerciseUi
import com.gympilot.ironlog.data.WorkoutPlan
import com.gympilot.ironlog.data.WorkoutPlanSummary
import com.gympilot.ironlog.ui.theme.IronDarkAccent
import com.gympilot.ironlog.ui.theme.IronPrimary
import com.gympilot.ironlog.ui.theme.IronSecondary
import com.gympilot.ironlog.viewmodel.WorkoutUiState
import com.gympilot.ironlog.viewmodel.WorkoutViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private val splitOptions = listOf("Push", "Pull", "Legs", "Upper", "Lower", "Bro Split", "Custom")
private val favoriteExercises = listOf(
    "Bench Press",
    "Squat",
    "Deadlift",
    "Overhead Press",
    "Lat Pulldown",
    "Seated Row",
    "Leg Press",
    "Biceps Curl"
)
private val WorkoutBackground = Color(0xFFF4F4F6)
private val WorkoutCardBorder = Color(0xFFE7E7EC)
private val WorkoutMuted = Color(0xFF858698)
private val WorkoutMutedDark = Color(0xFF595959)
private val WorkoutInput = Color(0xFFEDEDF2)
private val WorkoutGreen = Color(0xFF18A84F)

data class WorkoutSummary(
    val durationSeconds: Long,
    val completedCount: Int,
    val totalVolume: Double,
    val prCount: Int
)

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(rootPadding: PaddingValues, viewModel: WorkoutViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showAddExercise by remember { mutableStateOf(false) }
    var showSwitchPlan by remember { mutableStateOf(false) }
    var showCreatePlan by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<WorkoutSummary?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        containerColor = WorkoutBackground,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = rootPadding.calculateBottomPadding()),
                onClick = { showAddExercise = true },
                containerColor = WorkoutGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add exercise")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WorkoutBackground),
            contentPadding = PaddingValues(start = 18.dp, top = 28.dp, end = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WorkoutHeader(
                    selectedPlan = state.selectedPlan,
                    onSwitchPlans = { showSwitchPlan = true }
                )
            }
            item {
                CompactTimerCard(
                    state = state,
                    onStart = viewModel::startTimer,
                    onPause = viewModel::pauseTimer,
                    onResume = viewModel::resumeTimer,
                    onFinish = {
                        summary = state.toSummary()
                        viewModel.finishWorkout()
                    }
                )
            }
            item {
                PlanChips(
                    plans = state.plans,
                    selectedPlan = state.selectedPlan,
                    onSelect = viewModel::selectPlan,
                    onAddPlan = { showCreatePlan = true }
                )
            }
            itemsIndexed(state.exercises, key = { _, exercise -> exercise.id }) { _, exercise ->
                TrainingExerciseCard(
                    exercise = exercise,
                    onDone = { viewModel.markDone(exercise.id) },
                    onSkip = { viewModel.skipExercise(exercise.id) },
                    onSelectSet = { viewModel.selectSet(exercise.id, it) },
                    onUpdate = { name, weight, notes -> viewModel.updateExercise(exercise, name, weight, notes) },
                    onDelete = { viewModel.deleteExercise(exercise.id) }
                )
            }
            item {
                AddExerciseRow(onClick = { showAddExercise = true })
            }
        }
    }

    if (showAddExercise) {
        AddExerciseDialog(
            recent = state.exercises.map { it.name },
            onDismiss = { showAddExercise = false },
            onConfirm = {
                viewModel.addExercise(it)
                showAddExercise = false
            }
        )
    }
    if (showSwitchPlan) {
        SwitchPlanSheet(
            plans = state.planSummaries,
            selectedPlanId = state.selectedPlan?.id,
            onDismiss = { showSwitchPlan = false },
            onSelectPlan = {
                viewModel.selectPlan(it)
                showSwitchPlan = false
            },
            onCreatePlan = {
                showSwitchPlan = false
                showCreatePlan = true
            }
        )
    }
    if (showCreatePlan) {
        CreatePlanSheet(
            onDismiss = { showCreatePlan = false },
            onConfirm = { name, split ->
                viewModel.addPlan(name, split)
                showCreatePlan = false
            }
        )
    }
    summary?.let {
        WorkoutSummaryDialog(summary = it, onDismiss = { summary = null })
    }
}

@Composable
private fun WorkoutHeader(selectedPlan: WorkoutPlan?, onSwitchPlans: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    //Text("IronLog", fontSize = 30.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10111A))
                    Surface(shape = CircleShape, color = WorkoutGreen) {
                        Text(
                            "IronLog",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Text(
                    "Track Every Rep. Build Every PR.",
                    color = WorkoutMutedDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                onClick = onSwitchPlans,
                shape = CircleShape,
                color = Color(0xFFEDEDF2),
                border = BorderStroke(1.dp, Color(0xFFD7D7DE))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Switch", color = Color(0xFF11121B), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color(0xFF11121B))
                }
            }
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WorkoutCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 23.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Current workout", color = WorkoutMuted, fontSize = 16.sp)
                    Text(selectedPlan?.name ?: "No plan", color = Color(0xFF11121B), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }
                Surface(shape = CircleShape, color = Color(0xFFD8F0DE)) {
                    Text(
                        selectedPlan?.splitType.orEmpty().ifBlank { "Plan" },
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        color = WorkoutGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactTimerCard(
    state: WorkoutUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, WorkoutCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = Color(0xFFE9E9EF), modifier = Modifier.size(50.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = WorkoutMuted, modifier = Modifier.size(26.dp))
                }
            }
            AnimatedContent(targetState = state.elapsedSeconds, label = "timer") { elapsed ->
                Text(formatDuration(elapsed), color = Color(0xFF070812), fontSize = 38.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.weight(1f))
            when {
                !state.isRunning -> Button(
                    onClick = onStart,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoutGreen),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                ) { Text("Start", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
                state.isPaused -> Button(
                    onClick = onResume,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoutGreen),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp)
                ) { Text("Resume", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
                else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onPause,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE4D4F5),
                            contentColor = Color(0xFF11121B)
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text("Pause", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    FinishWorkoutButton(onClick = onFinish)
                }
            }
        }
    }
}

@Composable
private fun FinishWorkoutButton(onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = WorkoutGreen,
            contentColor = Color.White
        )
    ) {
        Icon(Icons.Filled.Check, contentDescription = "Finish workout", modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun PlanChips(
    plans: List<WorkoutPlan>,
    selectedPlan: WorkoutPlan?,
    onSelect: (Long) -> Unit,
    onAddPlan: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
        items(plans, key = { it.id }) { plan ->
            PlanPill(
                label = plan.splitType.takeIf { it.length <= 8 } ?: plan.name,
                selected = plan.id == selectedPlan?.id,
                onClick = { onSelect(plan.id) }
            )
        }
        item {
            Surface(onClick = onAddPlan, shape = CircleShape, color = Color(0xFFEAEAEE), modifier = Modifier.size(43.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = "Add plan", tint = WorkoutMuted, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun PlanPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) WorkoutGreen else Color(0xFFEAEAEE)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp),
            color = if (selected) Color.White else WorkoutMuted,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun TrainingExerciseCard(
    exercise: WorkoutExerciseUi,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onSelectSet: (Int) -> Unit,
    onUpdate: (String, Double, String) -> Unit,
    onDelete: () -> Unit
) {
    var weightText by remember(exercise.id) { mutableStateOf(weightToText(exercise.weight)) }
    var notesText by remember(exercise.id) { mutableStateOf(exercise.notes) }
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    val containerColor by animateColorAsState(
        targetValue = when {
            exercise.completed -> Color(0xFFF5FBF6)
            exercise.skipped -> Color(0xFFFAF5F5)
            else -> Color.White
        },
        animationSpec = spring(),
        label = "exerciseCompleteTint"
    )
    val borderColor = when {
        exercise.completed -> Color(0xFFD8F0DE)
        exercise.skipped -> Color(0xFFF0DADA)
        else -> WorkoutCardBorder
    }
    val supportingTextColor = WorkoutMuted

    LaunchedEffect(exercise.weight) {
        if (weightText.toDoubleOrNull() != exercise.weight) weightText = weightToText(exercise.weight)
    }
    LaunchedEffect(exercise.notes) {
        if (notesText != exercise.notes) notesText = exercise.notes
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CompletionButton(
                    completed = exercise.completed,
                    skipped = exercise.skipped,
                    onDone = onDone
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name,
                        color = Color(0xFF11121B),
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        when {
                            exercise.completed -> exercise.selectedSet?.let { "Done · Set $it" } ?: "Done"
                            exercise.skipped -> "Skipped"
                            else -> exercise.previousWeight?.let { "Prev: ${weightToText(it)} kg × 3" } ?: "Prev: —"
                        },
                        color = supportingTextColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (!exercise.skipped) {
                    TextButton(
                        onClick = onSkip,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Skip", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = { showRename = true }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Rename exercise", tint = WorkoutMuted, modifier = Modifier.size(21.dp))
                }
                IconButton(onClick = { showDelete = true }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete exercise", tint = WorkoutMuted, modifier = Modifier.size(21.dp))
                }
            }
            SetPills(
                count = if (exercise.name.contains("$", ignoreCase = true)) exercise.name.takeLast(1).toInt() else 3,
                selectedSet = exercise.selectedSet,
                onSelect = onSelectSet
            )
            WeightStepper(
                value = weightText,
                completed = exercise.completed,
                onValueChange = {
                    weightText = it
                    onUpdate(exercise.name, it.toDoubleOrNull() ?: 0.0, notesText)
                },
                onStep = { delta ->
                    val next = ((weightText.toDoubleOrNull() ?: 0.0) + delta).coerceAtLeast(0.0)
                    weightText = weightToText(next)
                    onUpdate(exercise.name, next, notesText)
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notesText,
                onValueChange = {
                    notesText = it
                    onUpdate(exercise.name, weightText.toDoubleOrNull() ?: 0.0, notesText)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note...", color = Color(0xFFBFC0CA), fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                minLines = 1,
                maxLines = 1,
                shape = RoundedCornerShape(16.dp),
                colors = pillTextFieldColors()
            )
        }
    }

    if (showRename) {
        RenameExerciseDialog(
            initialValue = exercise.name,
            onDismiss = { showRename = false },
            onConfirm = {
                onUpdate(it, weightText.toDoubleOrNull() ?: 0.0, notesText)
                showRename = false
            }
        )
    }
    if (showDelete) {
        DeleteConfirmationDialog(
            exerciseName = exercise.name,
            onDismiss = { showDelete = false },
            onDelete = {
                onDelete()
                showDelete = false
            }
        )
    }
}

@Composable
private fun CompletionButton(completed: Boolean, skipped: Boolean, onDone: () -> Unit) {
    val container = when {
        skipped -> Color(0xFFF0DADA)
        completed -> Color(0xFFD8F0DE)
        else -> Color(0xFFE8E8EC)
    }
    val content = when {
        skipped -> Color(0xFFB75B5B)
        completed -> WorkoutGreen
        else -> WorkoutMuted
    }
    FilledIconButton(
        onClick = onDone,
        modifier = Modifier.size(46.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = container,
            contentColor = content
        )
    ) {
        Icon(
            imageVector = if (skipped) Icons.Filled.Close else Icons.Filled.Check,
            contentDescription = if (skipped) "Mark exercise done" else "Mark exercise done",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SetPills(count: Int, selectedSet: Int?, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(count) { index ->
            val setNumber = index + 1
            val selected = selectedSet?.let { setNumber <= it } == true
            Surface(
                onClick = { onSelect(setNumber) },
                shape = RoundedCornerShape(12.dp),
                color = if (selected) Color(0xFFD8F0DE) else Color(0xFFF0F0F3),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        setNumber.toString(),
                        color = if (selected) WorkoutGreen else WorkoutMuted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightStepper(
    value: String,
    completed: Boolean,
    onValueChange: (String) -> Unit,
    onStep: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        FilledIconButton(
            onClick = { onStep(-2.5) },
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFFE9E9EF),
                contentColor = Color(0xFF11121B)
            )
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease weight", modifier = Modifier.size(24.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = { raw -> onValueChange(raw.filter { it.isDigit() || it == '.' }) },
            modifier = Modifier.weight(1f),
            suffix = {
                Text(
                    "kg",
                    color = WorkoutMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = Color(0xFF11121B),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp),
            colors = pillTextFieldColors()
        )
        FilledIconButton(
            onClick = { onStep(2.5) },
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = WorkoutGreen,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Increase weight", modifier = Modifier.size(27.dp))
        }
    }
}

@Composable
private fun pillTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = WorkoutInput,
    unfocusedContainerColor = WorkoutInput,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    cursorColor = IronPrimary
)

@Composable
private fun AddExerciseRow(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color(0xFFDADBE2))
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = WorkoutMuted, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Exercise", color = WorkoutMuted, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddExerciseDialog(
    recent: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val candidates = remember(recent, query) {
        (recent + favoriteExercises)
            .distinct()
            .filter { query.isBlank() || it.contains(query, ignoreCase = true) }
            .take(8)
    }
    IronDialog(
        title = "Add Exercise",
        subtitle = "Search your recent lifts or add a new movement without leaving the workout.",
        icon = Icons.Filled.Add,
        onDismiss = onDismiss,
        confirmLabel = if (query.isBlank()) "Create Exercise" else "Create \"$query\"",
        confirmEnabled = query.isNotBlank(),
        onConfirm = { onConfirm(query) }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text("Search or create") },
            singleLine = true
        )
        QuickSection("Recent", recent.take(4), Icons.Filled.History, onConfirm)
        QuickSection("Favorites", candidates, Icons.Filled.Star, onConfirm)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickSection(title: String, values: List<String>, icon: ImageVector, onSelect: (String) -> Unit) {
    if (values.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = IronDarkAccent, modifier = Modifier.size(16.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.distinct().forEach { value ->
                FilterChip(selected = false, onClick = { onSelect(value) }, label = { Text(value) })
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SwitchPlanSheet(
    plans: List<WorkoutPlanSummary>,
    selectedPlanId: Long?,
    onDismiss: () -> Unit,
    onSelectPlan: (Long) -> Unit,
    onCreatePlan: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Switch Plan", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF11121B))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plans.forEach { plan ->
                    PlanPill(
                        label = plan.splitType.takeIf { it.length <= 8 } ?: plan.name,
                        selected = plan.id == selectedPlanId,
                        onClick = { onSelectPlan(plan.id) }
                    )
                }
            }
            Button(
                onClick = onCreatePlan,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = WorkoutGreen),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Create Plan", fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreatePlanSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var split by remember { mutableStateOf(splitOptions.first()) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Create Plan", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF11121B))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Plan name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                splitOptions.forEach { option ->
                    PlanPill(label = option, selected = split == option, onClick = { split = option })
                }
            }
            Button(
                onClick = { onConfirm(name, split) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = WorkoutGreen),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Create", fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatePlanDialog(
    plans: List<WorkoutPlan>,
    selectedPlan: WorkoutPlan?,
    onDismiss: () -> Unit,
    onSelectPlan: (Long) -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var split by remember { mutableStateOf(splitOptions.first()) }
    IronDialog(
        title = "Switch Plan",
        subtitle = "Jump to another split, or create the next workout plan in one place.",
        icon = Icons.Filled.SwapHoriz,
        onDismiss = onDismiss,
        confirmLabel = "Create New Plan",
        confirmEnabled = name.isNotBlank(),
        onConfirm = { onConfirm(name, split) }
    ) {
        if (plans.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Available plans", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    plans.forEach { plan ->
                        FilterChip(
                            selected = plan.id == selectedPlan?.id,
                            onClick = { onSelectPlan(plan.id) },
                            label = { Text(plan.name) }
                        )
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Create new", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Plan name") },
                    singleLine = true
                )
            }
        }
        Text("Split type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            splitOptions.forEach {
                FilterChip(selected = split == it, onClick = { split = it }, label = { Text(it) })
            }
        }
    }
}

@Composable
private fun RenameExerciseDialog(initialValue: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf(initialValue) }
    IronDialog(
        title = "Rename Exercise",
        subtitle = "Keep names short and easy to scan mid-set.",
        icon = Icons.Filled.Edit,
        onDismiss = onDismiss,
        confirmLabel = "Rename",
        confirmEnabled = value.isNotBlank(),
        onConfirm = { onConfirm(value) }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Exercise name") },
            singleLine = true
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(exerciseName: String, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Delete Exercise", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = { Text("Remove $exerciseName from this workout plan?") },
        confirmButton = { Button(onClick = onDelete) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun WorkoutSummaryDialog(summary: WorkoutSummary, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = { Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = IronPrimary) },
        title = { Text("Workout Saved", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryRow("Duration", formatDuration(summary.durationSeconds))
                SummaryRow("Exercises completed", summary.completedCount.toString())
                SummaryRow("Total volume", "${weightToText(summary.totalVolume)} kg")
                SummaryRow("Personal records", summary.prCount.toString())
                Text("Saved locally on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun IronDialog(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onDismiss: () -> Unit,
    confirmLabel: String,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Surface(shape = CircleShape, color = IronSecondary.copy(alpha = 0.2f)) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = IronDarkAccent,
                    modifier = Modifier.padding(12.dp).size(24.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cancel")
            }
        }
    )
}

private fun WorkoutUiState.toSummary(): WorkoutSummary {
    val completed = exercises.filter { it.completed }
    return WorkoutSummary(
        durationSeconds = elapsedSeconds.coerceAtLeast(1),
        completedCount = completed.size,
        totalVolume = completed.sumOf { it.weight },
        prCount = completed.count { exercise ->
            val previous = exercise.previousWeight
            previous == null || exercise.weight > previous
        }
    )
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3_600
    val minutes = (seconds % 3_600) / 60
    val remainingSeconds = seconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, remainingSeconds)
    } else {
        "%02d:%02d".format(minutes, remainingSeconds)
    }
}

fun weightToText(weight: Double): String =
    if (weight % 1.0 == 0.0) weight.toInt().toString() else "%.1f".format(weight)
