package com.example.attentiontokenmanager.uii

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attentiontokenmanager.AppDatabase
import com.example.attentiontokenmanager.CalendarEventEntity
import com.example.attentiontokenmanager.analytics.AppCount
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    database: AppDatabase,
    onRefresh: () -> Unit = {}
) {
    var refreshTrigger by remember { mutableStateOf(0) }
    
    val currentCalendar = remember { Calendar.getInstance() }
    var displayedMonth by remember { mutableStateOf(currentCalendar.get(Calendar.MONTH)) }
    var displayedYear by remember { mutableStateOf(currentCalendar.get(Calendar.YEAR)) }
    
    val today = remember { Calendar.getInstance() }
    
    // YYYY-MM-DD
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var selectedDateStr by remember { mutableStateOf(dateFormat.format(today.time)) }
    
    val calendarEventDao = database.calendarEventDao()
    val eventDao = database.attentionEventDao()
    
    // Real-time Event Block Stats
    val blockedStats by produceState(initialValue = emptyList<AppCount>(), key1 = selectedDateStr, key2 = refreshTrigger) {
        val parts = selectedDateStr.split("-")
        if (parts.size == 3) {
            val cal = Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
            val startOfDay = cal.timeInMillis
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 23, 59, 59)
            val endOfDay = cal.timeInMillis
            value = eventDao.getBlockedAppsForDate(startOfDay, endOfDay)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    
    // Read scheduled events for selected date
    var selectedDateEvents by remember { mutableStateOf<List<CalendarEventEntity>>(emptyList()) }
    
    var allEventDates by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(selectedDateStr, refreshTrigger) {
        selectedDateEvents = calendarEventDao.getEventsForDate(selectedDateStr)
    }

    LaunchedEffect(displayedMonth, displayedYear, refreshTrigger) {
        val list = calendarEventDao.getAllEventDates()
        allEventDates = list.toSet()
    }

    var newEventName by remember { mutableStateOf("") }

    val monthNames = arrayOf("JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER")

    // For restricting past scheduling
    val selectedCal = Calendar.getInstance().apply {
        val parts = selectedDateStr.split("-")
        if (parts.size == 3) {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }
    }
    val isFutureOrToday = !selectedCal.before(today) || (
        selectedCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        selectedCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    )

    val context = LocalContext.current
    val pm = context.packageManager

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text("COGNITIVE SOVEREIGNTY", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { refreshTrigger++; onRefresh() }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // ── Header ──
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SYSTEM CAPABILITY TARGETS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Text("STRATEGIC FORECAST", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Assign events to dates. AI will ingest the event description using NLP heuristics and dynamically adjust local app token rates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Calendar Month Navigation ──
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        if (displayedMonth == 0) {
                            displayedMonth = 11; displayedYear--
                        } else {
                            displayedMonth--
                        }
                    }) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous Month", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Text(
                        "${monthNames[displayedMonth]} $displayedYear",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = {
                        if (displayedMonth == 11) {
                            displayedMonth = 0; displayedYear++
                        } else {
                            displayedMonth++
                        }
                    }) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next Month", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                val displayCalendar = Calendar.getInstance().apply {
                    clear()
                    set(Calendar.YEAR, displayedYear)
                    set(Calendar.MONTH, displayedMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                
                val daysInMonth = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDayOfWeek = displayCalendar.get(Calendar.DAY_OF_WEEK)

                val dayLabels = listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")
                
                // Days Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayLabels.forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 8.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Grid mapping (Mon=0, Sun=6)
                val startDayOffset = (firstDayOfWeek + 5) % 7 
                
                var dayCounter = 1
                val totalCells = startDayOffset + daysInMonth
                val rows = (totalCells + 6) / 7

                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            if (cellIndex < startDayOffset || dayCounter > daysInMonth) {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else {
                                val day = dayCounter
                                val dateStr = String.format("%04d-%02d-%02d", displayedYear, displayedMonth + 1, day)
                                val isSelected = dateStr == selectedDateStr
                                val isToday = day == today.get(Calendar.DAY_OF_MONTH) && displayedMonth == today.get(Calendar.MONTH) && displayedYear == today.get(Calendar.YEAR)
                                val hasEvent = allEventDates.contains(dateStr)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clickable { selectedDateStr = dateStr }
                                        .then(
                                            when {
                                                isSelected -> Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)).background(Color.Transparent)
                                                else -> Modifier.background(Color.Transparent)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = String.format("%02d", day),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isToday) MaterialTheme.colorScheme.primary else if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (hasEvent) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                        }
                                    }
                                }
                                dayCounter++
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Dynamic Live Block Metrics (For Selected Date) ──
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                                                
                        Text("NOTIFICATIONS BLOCKED ON THIS DATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (blockedStats.isEmpty()) {
                            Text("No notifications were restricted on this day.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            blockedStats.forEach { stat ->
                                val appName = try {
                                    val info = pm.getApplicationInfo(stat.packageName, 0)
                                    pm.getApplicationLabel(info).toString()
                                } catch (e: Exception) { stat.packageName.substringAfterLast('.') }

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                                        Text(appName.firstOrNull()?.toString()?.uppercase() ?: "?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(appName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                                        Text("${stat.count} blocks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        // ── Event Scheduling Form (Heuristic Based) ──
                        if (isFutureOrToday) {
                            Text("SCHEDULE HEURISTIC EVENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = newEventName,
                                onValueChange = { newEventName = it },
                                placeholder = { Text("e.g. 'Study for exam', 'Chill time'", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                ),
                                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    if (newEventName.isNotBlank()) {
                                        coroutineScope.launch {
                                            database.calendarEventDao().insert(
                                                CalendarEventEntity(
                                                    date = selectedDateStr,
                                                    eventType = "HEURISTIC",
                                                    eventName = newEventName
                                                )
                                            )
                                            newEventName = ""
                                            refreshTrigger++
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("SCHEDULE ALLOCATION", color = MaterialTheme.colorScheme.background, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (selectedDateEvents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("SCHEDULED EVENTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            selectedDateEvents.forEach { event ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).padding(12.dp)
                                ) {
                                    Column {
                                        Text(event.eventName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                                        Text("Awaiting AI Analysis", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
