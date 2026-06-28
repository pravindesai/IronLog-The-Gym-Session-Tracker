package com.gympilot.ironlog.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

private val splitOptions = listOf("Push", "Pull", "Legs", "Upper", "Lower", "Bro Split", "Custom")
private val favoriteExercises = listOf(
    "Bench Press", "Squat", "Deadlift", "Overhead Press",
    "Lat Pulldown", "Seated Row", "Leg Press", "Biceps Curl"
)

private val WorkoutBackground = Color(0xFFF9FAFA)
private val WorkoutCardBorder = Color(0xFFE8E8E8)
private val WorkoutMuted = Color(0xFF6E7681)
private val WorkoutInk = Color(0xFF111B29)
private val WorkoutGreen = Color(0xFF4F7D5B)
private val WorkoutSoftGreen = Color(0xFFEAF2EB)
private val WorkoutLilac = Color(0xFFF0E8FF)
private val WorkoutSkip = Color(0xFFFC095D)

data class WorkoutSummary(
    val durationSeconds: Long,
    val completedCount: Int,
    val totalVolume: Double,
    val prCount: Int
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(rootPadding: PaddingValues, viewModel: WorkoutViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showAddExercise by remember { mutableStateOf(false) }
    var showSwitchPlan by remember { mutableStateOf(false) }
    var showCreatePlan by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<WorkoutSummary?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WorkoutBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(bottom = rootPadding.calculateBottomPadding() + 16.dp),
                onClick = { showAddExercise = true },
                containerColor = WorkoutGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Exercise", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        val isReorder = viewModel.reOrder.collectAsState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WorkoutBackground),
            contentPadding = PaddingValues(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WorkoutHeader(
                    selectedPlan = state.selectedPlan,
                    exerciseCount = state.exercises.size,
                    onSwitchPlans = { showSwitchPlan = true }
                )
            }
            item {
                TimerCard(
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exercises", color = WorkoutInk, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.reorderExercises()
                        }
                    ) {
                        Text("Reorder", color = WorkoutMuted, fontSize = 14.sp)
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, tint = WorkoutMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            itemsIndexed(if (isReorder.value) state.exercises.reversed() else state.exercises, key = { _, exercise -> exercise.id }) { _, exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onDone = { viewModel.markDone(exercise.id) },
                    onSelectSet = { viewModel.selectSet(exercise.id, it) },
                    onUpdate = { name, weight, notes -> viewModel.updateExercise(exercise, name, weight, notes) },
                    skipExercise = { viewModel.skipExercise(exercise.id) }
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(100.dp)
                )
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
private fun WorkoutHeader(selectedPlan: WorkoutPlan?, exerciseCount: Int, onSwitchPlans: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("IronLog", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = WorkoutInk)
                Text("Track Every Rep. Build Every PR.", color = WorkoutMuted, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSwitchPlans() }) {
                    Surface(
                        shape = CircleShape,
                        color = WorkoutLilac,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = WorkoutInk)
                        }
                    }
                    Text("Switch", color = WorkoutInk, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, border = BorderStroke(1.dp, WorkoutCardBorder)) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = WorkoutSoftGreen, modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("💪", fontSize = 28.sp)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Current Workout", color = WorkoutMuted, fontSize = 14.sp)
                    Text(selectedPlan?.name ?: "Rest Day", color = WorkoutInk, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = WorkoutInk, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("$exerciseCount Exercises", color = WorkoutInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = WorkoutInk, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Today", color = WorkoutInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = WorkoutInk)
            }
        }
    }
}

@Composable
private fun TimerCard(
    state: WorkoutUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color.White, border = BorderStroke(1.dp, WorkoutCardBorder)) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = WorkoutSoftGreen, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = WorkoutGreen, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.elapsedSeconds, 
                    label = "timer", 
                    transitionSpec = {
                        slideInVertically(
                            initialOffsetY = { -it }, // Start above
                            animationSpec = tween(500)
                        ) + fadeIn() togetherWith
                                slideOutVertically(
                                    targetOffsetY = { it }, // Exit below
                                    animationSpec = tween(500)
                                ) + fadeOut()
                    }
                    ) { elapsed ->
                    Text(formatDuration(elapsed), color = WorkoutInk, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(if (state.isRunning) "Workout in progress" else "Ready to start", color = WorkoutGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            if (!state.isRunning) {
                Button(onClick = onStart, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = WorkoutGreen)) {
                    Text("Start", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            onClick = if (state.isPaused) onResume else onPause,
                            shape = CircleShape,
                            color = WorkoutLilac,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(if (state.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, contentDescription = null, tint = WorkoutInk)
                            }
                        }
                        Text(if (state.isPaused) "Resume" else "Pause", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoutInk)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(onClick = onFinish, shape = CircleShape, color = WorkoutGreen, modifier = Modifier.size(56.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Stop, contentDescription = null, tint = Color.White)
                            }
                        }
                        Text("Finish", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoutInk)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: WorkoutExerciseUi,
    onDone: () -> Unit,
    onSelectSet: (Int) -> Unit,
    onUpdate: (String, Double, String) -> Unit,
    skipExercise: () -> Unit
) {
    var weightText by remember(exercise.id) { mutableStateOf(weightToText(exercise.weight)) }
    var notesText by remember(exercise.id) { mutableStateOf(exercise.notes) }
    var expanded by remember { mutableStateOf(false) }

    val containerColor by animateColorAsState(
        targetValue = if (exercise.completed) Color(0xFFF9FAFA) else Color.White,
        label = "exerciseTint"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(1.dp, if (exercise.completed) WorkoutGreen.copy(alpha = 0.3f) else WorkoutCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onDone,
                    shape = CircleShape,
                    color = if (exercise.completed) WorkoutGreen else Color.White,
                    border = if (exercise.completed) null else BorderStroke(1.dp, WorkoutCardBorder),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (exercise.completed) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name,
                        color = WorkoutInk,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 4,
                        lineHeight = 22.sp,
                        overflow = TextOverflow.Visible
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Last: ${weightToText(exercise.previousWeight ?: 0.0)} kg \u00d7 8", color = WorkoutMuted, fontSize = 13.sp)
                        Surface(shape = RoundedCornerShape(4.dp), color = WorkoutSoftGreen) {
                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = WorkoutGreen, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("PR ${weightToText(maxOf(exercise.previousWeight ?: 0.0, exercise.weight))} kg", color = WorkoutGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                IconButton(onClick = skipExercise) {
                    Icon(Icons.Filled.SkipNext, contentDescription = null, tint = WorkoutSkip)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("-2.5", "-1").forEach { label ->
                    WeightAdjustmentButton(label = label) {
                        val delta = label.toDoubleOrNull() ?: 0.0
                        val next = ((weightText.toDoubleOrNull() ?: 0.0) + delta).coerceAtLeast(0.0)
                        weightText = weightToText(next)
                        onUpdate(exercise.name, next, notesText)
                    }
                }
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { raw ->
                        weightText = raw.filter { it.isDigit() || it == '.' }
                        onUpdate(exercise.name, weightText.toDoubleOrNull() ?: 0.0, notesText)
                    },
                    modifier = Modifier.weight(1.5f),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
                    suffix = { Text("kg", color = WorkoutMuted, fontSize = 14.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = WorkoutCardBorder,
                        focusedBorderColor = WorkoutGreen,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                listOf("+1", "+2.5").forEach { label ->
                    WeightAdjustmentButton(label = label) {
                        val delta = label.toDoubleOrNull() ?: 0.0
                        val next = ((weightText.toDoubleOrNull() ?: 0.0) + delta).coerceAtLeast(0.0)
                        weightText = weightToText(next)
                        onUpdate(exercise.name, next, notesText)
                    }
                }
            }

            // SetPills added below weight input as requested
            SetPills(
                count = if (exercise.name.contains("$", ignoreCase = true)) exercise.name.takeLast(1).toIntOrNull() ?: 3 else 3,
                selectedSet = exercise.selectedSet,
                onSelect = onSelectSet
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = WorkoutMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Notes", color = WorkoutInk, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = WorkoutInk)
                }
                if (expanded) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = {
                            notesText = it
                            onUpdate(exercise.name, weightText.toDoubleOrNull() ?: 0.0, notesText)
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = { Text("Add notes...", fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = WorkoutCardBorder,
                            focusedBorderColor = WorkoutGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
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
                color = if (selected) WorkoutSoftGreen else Color(0xFFF4F4F4),
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
private fun WeightAdjustmentButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF4F4F4),
        modifier = Modifier.size(width = 54.dp, height = 44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = WorkoutInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
        subtitle = "Search your recent lifts or add a new movement.",
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                    Surface(
                        onClick = { onSelectPlan(plan.id) },
                        shape = CircleShape,
                        color = if (plan.id == selectedPlanId) WorkoutGreen else Color(0xFFEAEAEE)
                    ) {
                        Text(
                            plan.name,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp),
                            color = if (plan.id == selectedPlanId) Color.White else WorkoutMuted,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                    Surface(
                        onClick = { split = option },
                        shape = CircleShape,
                        color = if (split == option) WorkoutGreen else Color(0xFFEAEAEE)
                    ) {
                        Text(
                            option,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp),
                            color = if (split == option) Color.White else WorkoutMuted,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
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
