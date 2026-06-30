package com.gympilot.ironlog.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gympilot.ironlog.data.HistoryLogItem
import com.gympilot.ironlog.data.SessionWithVolume
import com.gympilot.ironlog.viewmodel.ProgressUiState
import com.gympilot.ironlog.viewmodel.ProgressViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ProgressBackground = Color(0xFFFAFAF8)
private val ProgressInk = Color(0xFF111B29)
private val ProgressMuted = Color(0xFF5C6670)
private val ProgressGreen = Color(0xFF4F7D5B)
private val ProgressSoftGreen = Color(0xFFEAF2EB)
private val ProgressLilac = Color(0xFFF0E8FF)
private val ProgressBorder = Color(0xFFE8E8E5)

@Composable
fun ProgressScreen(rootPadding: PaddingValues, viewModel: ProgressViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val topLifts = remember(state.logsBySession) { topLifts(state.logsBySession.values.flatten()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(rootPadding)
            .background(ProgressBackground),
        contentPadding = PaddingValues(start = 18.dp, top = 28.dp, end = 18.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProgressHeader() }
        item { Text("Overview", color = ProgressInk, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold) }
        item {
            MetricGrid(
                streak = state.currentStreak,
                maxStreak = state.maxStreak,
                workouts = state.totalWorkouts,
                workoutsThisMonth = state.workoutsThisMonth,
                averageDurationSeconds = state.averageDurationSeconds,
                avgDurationThisMonth = state.averageDurationSecondsThisMonth,
                totalVolume = state.totalVolume,
                totalVolumeThisMonth = state.totalVolumeThisMonth
            )
        }
        item {
            WeightProgressCard(
                selectedExercise = state.selectedExercise,
                selectedRange = state.selectedRange,
                onRangeSelect = viewModel::selectRange,
                values = state.exerciseProgress.map { it.weight },
                onExerciseClick = {
                    val names = state.exerciseNames
                    if (names.isNotEmpty()) {
                        val index = names.indexOf(state.selectedExercise).coerceAtLeast(0)
                        viewModel.selectExercise(names[(index + 1) % names.size])
                    }
                }
            )
        }
        if (topLifts.isNotEmpty()) {
            item {
                PrHighlights(topLifts = topLifts)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FrequencyCard(
                    title = "Weekly Frequency",
                    subtitle = "Workouts per day",
                    labels = state.weeklyLabels,
                    values = state.weeklyFrequency.map { it.toDouble() },
                    modifier = Modifier.weight(1f)
                )
                FrequencyCard(
                    title = "Monthly Workouts",
                    subtitle = "This year",
                    labels = state.monthlyLabels,
                    values = state.monthlyWorkouts.map { it.toDouble() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (topLifts.isNotEmpty()) {
            item {
                TopLiftsCard(topLifts = topLifts)
            }
        }
        item {
            VolumeCard(totalVolume = state.totalVolume, values = state.volumeOverTime)
        }
        item { Text("Workout History", color = ProgressInk, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold) }
        if (state.sessions.isEmpty()) {
            item { EmptyHistoryCard() }
        } else {
            items(state.sessions, key = { it.id }) { session ->
                HistoryCard(session = session, logs = state.logsBySession[session.id].orEmpty())
            }
        }
    }
}

@Composable
private fun ProgressHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Progress", color = ProgressInk, fontSize = 31.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold)
        Text("Trends that help the next set.", color = ProgressMuted, fontSize = 17.sp)
    }
}

@Composable
private fun MetricGrid(
    streak: Int,
    maxStreak: Int,
    workouts: Int,
    workoutsThisMonth: Int,
    averageDurationSeconds: Long,
    avgDurationThisMonth: Long,
    totalVolume: Double,
    totalVolumeThisMonth: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("Streak", streak.toString(), "days", "Best: $maxStreak", Icons.Filled.LocalFireDepartment, Modifier.weight(1f))
            MetricTile("Workouts", workouts.toString(), "total", "$workoutsThisMonth this month", Icons.Filled.FitnessCenter, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val avgMin = (averageDurationSeconds / 60)
            val monthAvgMin = (avgDurationThisMonth / 60)
            MetricTile("Avg. Duration", avgMin.toString(), "min", "$monthAvgMin min this month", Icons.Filled.Timer, Modifier.weight(1f))
            MetricTile("Total Volume", compactNumber(totalVolume), "kg", "${compactNumber(totalVolumeThisMonth)} this month", Icons.Filled.Scale, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, unit: String, footer: String, icon: ImageVector, modifier: Modifier = Modifier) {
    SoftCard(modifier = modifier.height(180.dp)) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            IconBubble(icon, tint = ProgressGreen, background = ProgressSoftGreen, size = 38.dp)
            Text(label, color = ProgressMuted, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(value, color = ProgressInk, fontSize = 37.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(unit, color = ProgressMuted, fontSize = 15.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
            Text(footer, color = if (footer.startsWith("Best")) ProgressGreen else ProgressMuted, fontSize = 14.sp, maxLines = 1)
        }
    }
}

@Composable
private fun WeightProgressCard(
    selectedExercise: String,
    selectedRange: String,
    onRangeSelect: (String) -> Unit,
    values: List<Double>,
    onExerciseClick: () -> Unit
) {
    SoftCard {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QueryStats, contentDescription = null, tint = ProgressGreen, modifier = Modifier.size(27.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Weight Progression", color = ProgressInk, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Track your lifts over time", color = ProgressMuted, fontSize = 14.sp)
                }
                if (selectedExercise.isNotEmpty()) {
                    Surface(onClick = onExerciseClick, shape = CircleShape, color = Color(0xFFF5F6F3)) {
                        Text(
                            selectedExercise,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            color = ProgressInk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (values.isNotEmpty()) {
                RangeTabs(selectedRange = selectedRange, onSelect = onRangeSelect)
                LineChart(values = values, modifier = Modifier.height(190.dp))
            } else {
                Box(modifier = Modifier.height(190.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No data for ${selectedExercise.ifEmpty { "this exercise" }}", color = ProgressMuted, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun RangeTabs(selectedRange: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        listOf("1W", "1M", "3M", "6M", "1Y", "All").forEach { label ->
            val selected = label == selectedRange
            Surface(
                onClick = { onSelect(label) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected) Color(0xFFE2E7DF) else Color.Transparent
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                    color = ProgressMuted,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PrHighlights(topLifts: List<TopLift>) {
    val lifts = topLifts.take(3)
    SoftCard {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.Scale, tint = ProgressGreen, background = ProgressSoftGreen, size = 34.dp)
                Spacer(Modifier.width(10.dp))
                Text("PR Highlights", color = ProgressInk, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Text("›", color = ProgressInk, fontSize = 28.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                lifts.forEach { lift ->
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(lift.name, color = ProgressMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${weightToText(lift.weight)} kg", color = ProgressInk, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Personal Record", color = Color(0xFF119C45), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequencyCard(title: String, subtitle: String, labels: List<String>, values: List<Double>, modifier: Modifier = Modifier) {
    SoftCard(modifier = modifier.height(220.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = ProgressInk, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = ProgressMuted, fontSize = 12.sp)
            if (values.any { it > 0 }) {
                BarChart(values = values, labels = labels, modifier = Modifier.weight(1f))
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No data", color = ProgressMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TopLiftsCard(topLifts: List<TopLift>) {
    val lifts = topLifts.take(4)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Top Lifts (PRs)", color = ProgressInk, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Text("View all", color = ProgressGreen, fontSize = 15.sp)
        }
        SoftCard {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
                lifts.forEach { lift ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBubble(Icons.Filled.FitnessCenter, tint = ProgressGreen, background = ProgressSoftGreen, size = 36.dp)
                        Spacer(Modifier.width(14.dp))
                        Text(lift.name, color = ProgressInk, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${weightToText(lift.weight)} kg", color = ProgressInk, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumeCard(totalVolume: Double, values: List<Double>) {
    SoftCard {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QueryStats, contentDescription = null, tint = ProgressGreen, modifier = Modifier.size(27.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Volume Over Time", color = ProgressInk, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Total volume (kg)", color = ProgressMuted, fontSize = 13.sp)
                }
                Surface(shape = CircleShape, color = Color(0xFFF5F6F3)) {
                    Text("All Time", modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), color = ProgressInk, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(compactNumber(totalVolume), color = ProgressInk, fontSize = 31.sp, fontWeight = FontWeight.ExtraBold)
                Text("kg", color = ProgressMuted, fontSize = 16.sp, modifier = Modifier.padding(bottom = 5.dp))
            }
            if (values.size > 1) {
                LineChart(values = values, modifier = Modifier.height(160.dp), fill = true)
            } else {
                Box(modifier = Modifier.height(160.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("More workouts needed for trends", color = ProgressMuted, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    SoftCard {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("No completed workouts yet.", color = ProgressInk, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text("Finish a workout to unlock your history.", color = ProgressMuted)
        }
    }
}

@Composable
private fun HistoryCard(session: SessionWithVolume, logs: List<HistoryLogItem>) {
    var expanded by remember { mutableStateOf(false) }
    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Filled.FitnessCenter, tint = ProgressGreen, background = ProgressLilac, size = 42.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(formatSessionDate(session.finishedAt), color = ProgressMuted, fontSize = 12.sp)
                    Text(session.workoutName, color = ProgressInk, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${logs.count { it.completed }} exercises • ${weightToText(session.volume)} kg", color = ProgressMuted, fontSize = 13.sp)
                }
                Text(formatDuration(session.durationSeconds), color = ProgressMuted, fontSize = 16.sp)
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = ProgressInk)
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    logs.forEach { log ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.exerciseName, color = ProgressInk, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text("${weightToText(log.weight)} kg", color = ProgressInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SoftCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ProgressBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
private fun IconBubble(icon: ImageVector, tint: Color, background: Color = Color.White, size: androidx.compose.ui.unit.Dp) {
    Surface(shape = RoundedCornerShape(15.dp), color = background, border = if (background == Color.White) BorderStroke(1.dp, ProgressBorder) else null, modifier = Modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.52f))
        }
    }
}

@Composable
private fun LineChart(values: List<Double>, modifier: Modifier = Modifier, fill: Boolean = false) {
    val progress by animateFloatAsState(targetValue = 1f, label = "progressLine")
    Canvas(modifier = modifier.fillMaxWidth()) {
        val max = values.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
        val min = values.minOrNull() ?: 0.0
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val chartHeight = size.height * 0.78f
        val top = size.height * 0.08f
        val step = if (values.size <= 1) size.width else size.width / (values.size - 1)
        val visibleCount = (values.size * progress).toInt().coerceIn(1, values.size)
        val points = values.take(visibleCount).mapIndexed { index, value ->
            val x = if (values.size == 1) size.width / 2f else index * step
            val y = top + chartHeight - (((value - min) / range).toFloat() * chartHeight).coerceIn(0f, chartHeight)
            Offset(x, y)
        }
        repeat(4) { i ->
            val y = top + (chartHeight / 3f) * i
            drawLine(Color(0xFFEAEDEA), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        if (points.size > 1) {
            if (fill) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, size.height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, size.height)
                    close()
                }
                drawPath(fillPath, color = ProgressGreen.copy(alpha = 0.18f))
            }
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, color = ProgressGreen, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            points.forEach { drawCircle(color = ProgressGreen, radius = 4.dp.toPx(), center = it) }
        } else if (points.size == 1) {
            drawCircle(color = ProgressGreen, radius = 6.dp.toPx(), center = points.first())
        }
    }
}

@Composable
private fun BarChart(values: List<Double>, labels: List<String>, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(targetValue = 1f, label = "progressBars")
    Column(modifier = modifier, verticalArrangement = Arrangement.Bottom) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val max = values.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
            val gap = 11.dp.toPx()
            val barWidth = (size.width - gap * (values.size - 1)) / values.size.coerceAtLeast(1)
            values.forEachIndexed { index, value ->
                val h = ((value / max).toFloat() * size.height * progress).coerceAtLeast(2.dp.toPx())
                drawRoundRect(
                    color = if (value > 0) ProgressGreen else Color(0xFFE3E8E5),
                    topLeft = Offset(index * (barWidth + gap), size.height - h),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { Text(it, color = ProgressInk, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.width(20.dp)) }
        }
    }
}

private data class TopLift(val name: String, val weight: Double)

private fun topLifts(logs: List<HistoryLogItem>): List<TopLift> =
    logs
        .filter { it.completed }
        .groupBy { it.exerciseName }
        .map { (name, rows) -> TopLift(name, rows.maxOf { it.weight }) }
        .sortedByDescending { it.weight }

private fun compactNumber(value: Double): String =
    if (value >= 1_000) "%,.0f".format(value) else weightToText(value)

private fun formatSessionDate(epochMillis: Long): String {
    val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}
