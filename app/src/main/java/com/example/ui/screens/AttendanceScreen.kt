package com.example.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Attendance
import com.example.data.local.Student
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PendingAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    students: List<Student>,
    attendanceList: List<Attendance>,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    selectedBatch: String,
    onBatchChange: (String) -> Unit,
    onMarkStatus: (studentId: Long, status: String, batch: String) -> Unit,
    onMarkAllPresent: (batch: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

    val currentDateParsed = remember(selectedDate) {
        try {
            dateFormat.parse(selectedDate) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    val availableBatches = remember(students) {
        listOf("All") + students.map { it.batch }.distinct()
    }

    val filteredStudents = remember(students, selectedBatch) {
        if (selectedBatch == "All") students else students.filter { it.batch == selectedBatch }
    }

    // Attendance stats for filtered list
    val presentCount = remember(filteredStudents, attendanceList) {
        filteredStudents.count { s ->
            attendanceList.any { it.studentId == s.id && it.status == "PRESENT" }
        }
    }
    val absentCount = remember(filteredStudents, attendanceList) {
        filteredStudents.count { s ->
            attendanceList.any { it.studentId == s.id && it.status == "ABSENT" }
        }
    }
    val lateCount = remember(filteredStudents, attendanceList) {
        filteredStudents.count { s ->
            attendanceList.any { it.studentId == s.id && it.status == "LATE" }
        }
    }

    fun shiftDate(days: Int) {
        val cal = Calendar.getInstance()
        cal.time = currentDateParsed
        cal.add(Calendar.DAY_OF_YEAR, days)
        onDateChange(dateFormat.format(cal.time))
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Sticky Header: Date Selector + Inline Attendance Stat Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { shiftDate(-1) }, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Day", modifier = Modifier.size(15.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = displayFormat.format(currentDateParsed),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = { shiftDate(1) }, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day", modifier = Modifier.size(15.dp))
                }
            }

            // Compact Inline Stat Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓ $presentCount", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PaidGreen)
                    Text("✗ $absentCount", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OverdueRed)
                    Text("⌛ $lateCount", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PendingAmber)
                }
            }
        }

        // Horizontal Scrollable Options Bar (Mark All + Batches)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .height(28.dp)
                        .clickable { onMarkAllPresent(selectedBatch) }
                        .testTag("mark_all_present_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Mark All Present", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            items(availableBatches) { b ->
                FilterChip(
                    selected = selectedBatch == b,
                    onClick = { onBatchChange(b) },
                    label = { Text(b, fontSize = 10.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // Student Roster (Maximized Workspace)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (filteredStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No students in this batch",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    }
                }
            } else {
                items(filteredStudents, key = { it.id }) { student ->
                    val record = attendanceList.find { it.studentId == student.id }
                    AttendanceStudentRow(
                        student = student,
                        currentStatus = record?.status ?: "UNMARKED",
                        onMarkStatus = { status ->
                            onMarkStatus(student.id, status, student.batch)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceStat(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AttendanceStudentRow(
    student: Student,
    currentStatus: String,
    onMarkStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(name = student.name, photoUri = student.photoUri, size = 36.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${student.gradeClass}${if (student.rollNo.isNotEmpty()) " (${student.rollNo})" else ""}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Compact Status Buttons (Present, Late, Absent)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AttendanceStatusButton(
                    label = "P",
                    isSelected = currentStatus == "PRESENT",
                    activeColor = PaidGreen,
                    onClick = { onMarkStatus("PRESENT") }
                )

                AttendanceStatusButton(
                    label = "L",
                    isSelected = currentStatus == "LATE",
                    activeColor = PendingAmber,
                    onClick = { onMarkStatus("LATE") }
                )

                AttendanceStatusButton(
                    label = "A",
                    isSelected = currentStatus == "ABSENT",
                    activeColor = OverdueRed,
                    onClick = { onMarkStatus("ABSENT") }
                )
            }
        }
    }
}

@Composable
private fun AttendanceStatusButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(30.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
