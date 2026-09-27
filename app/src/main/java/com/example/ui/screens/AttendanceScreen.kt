package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.clip
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Date Navigator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { shiftDate(-1) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Day")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayFormat.format(currentDateParsed),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                IconButton(onClick = { shiftDate(1) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Batch Filter Chips
        if (availableBatches.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableBatches) { b ->
                    FilterChip(
                        selected = selectedBatch == b,
                        onClick = { onBatchChange(b) },
                        label = { Text(b, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Summary Counters & Mark All Action
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AttendanceStat(label = "Present", count = presentCount, color = PaidGreen)
                    AttendanceStat(label = "Absent", count = absentCount, color = OverdueRed)
                    AttendanceStat(label = "Late", count = lateCount, color = PendingAmber)
                    AttendanceStat(label = "Total", count = filteredStudents.size, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onMarkAllPresent(selectedBatch) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mark_all_present_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark All Present")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Student Roster
        LazyColumn(
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredStudents, key = { it.id }) { student ->
                val record = attendanceList.find { it.studentId == student.id }
                val currentStatus = record?.status ?: "UNMARKED"

                AttendanceStudentRow(
                    student = student,
                    status = currentStatus,
                    onStatusSelected = { status ->
                        onMarkStatus(student.id, status, student.batch)
                    }
                )
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
            fontSize = 18.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AttendanceStudentRow(
    student: Student,
    status: String,
    onStatusSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(name = student.name, photoUri = student.photoUri, size = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${student.gradeClass} • ${student.batch}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // P, A, L Toggle Pills
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(
                    letter = "P",
                    isSelected = status == "PRESENT",
                    activeColor = PaidGreen,
                    onClick = { onStatusSelected("PRESENT") }
                )
                StatusPill(
                    letter = "A",
                    isSelected = status == "ABSENT",
                    activeColor = OverdueRed,
                    onClick = { onStatusSelected("ABSENT") }
                )
                StatusPill(
                    letter = "L",
                    isSelected = status == "LATE",
                    activeColor = PendingAmber,
                    onClick = { onStatusSelected("LATE") }
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    letter: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}
