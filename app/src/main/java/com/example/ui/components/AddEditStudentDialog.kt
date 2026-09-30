package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Tune
import com.example.data.local.BatchItem
import com.example.data.local.Student
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditStudentDialog(
    studentToEdit: Student?,
    batches: List<BatchItem> = emptyList(),
    onManageBatches: () -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    BackHandler(onBack = onDismiss)

    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var rollNo by remember { mutableStateOf(studentToEdit?.rollNo ?: "") }
    var gradeClass by remember { mutableStateOf(studentToEdit?.gradeClass ?: "Class 10") }
    var batch by remember { mutableStateOf(studentToEdit?.batch ?: "Class 10 Board Masters (06:30 PM - 08:00 PM)") }
    var phone by remember { mutableStateOf(studentToEdit?.phone ?: "") }
    var parentPhone by remember { mutableStateOf(studentToEdit?.parentPhone ?: "") }
    var monthlyFee by remember { mutableStateOf(studentToEdit?.monthlyFee?.toInt()?.toString() ?: "1500") }
    var dueDay by remember { mutableStateOf(studentToEdit?.dueDay?.toString() ?: "5") }
    var admissionDate by remember {
        mutableStateOf(studentToEdit?.admissionDate.takeIf { !it.isNullOrEmpty() }
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var photoUri by remember { mutableStateOf(studentToEdit?.photoUri) }
    var notes by remember { mutableStateOf(studentToEdit?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var feeError by remember { mutableStateOf(false) }

    // Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    val standardClasses = listOf(
        "Class KG",
        "Class 1",
        "Class 2",
        "Class 3",
        "Class 4",
        "Class 5",
        "Class 6",
        "Class 7",
        "Class 8",
        "Class 9",
        "Class 10"
    )

    val commonClasses = remember(batches) {
        val extraClasses = batches.map { it.gradeClass }.filter { it.isNotBlank() }
        (standardClasses + extraClasses).distinct()
    }
    var classMenuExpanded by remember { mutableStateOf(false) }

    val timingPresets = listOf(
        "12:00 PM - 01:00 PM",
        "12:00 PM - 01:30 PM",
        "01:00 PM - 02:30 PM",
        "02:00 PM - 03:30 PM",
        "03:00 PM - 04:30 PM",
        "04:00 PM - 05:30 PM",
        "05:00 PM - 06:30 PM",
        "06:00 PM - 07:30 PM",
        "06:30 PM - 08:00 PM",
        "07:00 PM - 08:00 PM"
    )
    var batchMenuExpanded by remember { mutableStateOf(false) }

    fun submitForm() {
        val isNameValid = name.isNotBlank()
        val isPhoneValid = phone.isNotBlank()
        val feeVal = monthlyFee.toDoubleOrNull()
        val isFeeValid = feeVal != null && feeVal >= 0

        nameError = !isNameValid
        phoneError = !isPhoneValid
        feeError = !isFeeValid

        if (isNameValid && isPhoneValid && isFeeValid) {
            val parsedDueDay = dueDay.toIntOrNull()?.coerceIn(1, 28) ?: 5
            val student = Student(
                id = studentToEdit?.id ?: 0L,
                name = name.trim(),
                rollNo = rollNo.trim(),
                gradeClass = gradeClass.trim(),
                batch = batch.trim(),
                phone = phone.trim(),
                parentPhone = parentPhone.trim(),
                monthlyFee = feeVal!!,
                dueDay = parsedDueDay,
                admissionDate = admissionDate.trim(),
                photoUri = photoUri,
                active = true,
                notes = notes.trim()
            )
            onSave(student)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WindowInsets.statusBars.asPaddingValues())
                    ) {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = if (studentToEdit == null) "New Student Admission" else "Edit Student Profile",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Complete details & fee schedule",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Close"
                                    )
                                }
                            },
                            actions = {
                                Button(
                                    onClick = { submitForm() },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .testTag("save_student_button_top"),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (studentToEdit == null) Icons.Default.PersonAdd else Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (studentToEdit == null) "Add Student" else "Save")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Card 1: Student Avatar & Basic Identity
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                            ) {
                                StudentAvatar(
                                    name = name.ifEmpty { "Student" },
                                    photoUri = photoUri,
                                    size = 72.dp
                                )
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(26.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Change photo",
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Student Photo",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Tap circle to choose from gallery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!photoUri.isNullOrEmpty()) {
                                    TextButton(
                                        onClick = { photoUri = null },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove photo",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Remove Photo", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = it.isBlank()
                            },
                            label = { Text("Full Name *") },
                            placeholder = { Text("e.g. Rahul Sharma") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            isError = nameError,
                            supportingText = if (nameError) { { Text("Name is required") } } else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = rollNo,
                            onValueChange = { rollNo = it },
                            label = { Text("Roll No. / Student ID") },
                            placeholder = { Text("e.g. 10A-14") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 2: Academic Course & Batch
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Course & Batch Schedule",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                            TextButton(
                                onClick = onManageBatches,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Manage Batches", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Class Dropdown / Input
                        ExposedDropdownMenuBox(
                            expanded = classMenuExpanded,
                            onExpandedChange = { classMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = gradeClass,
                                onValueChange = { gradeClass = it },
                                label = { Text("Class (KG - 10 or Custom) *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            )
                            ExposedDropdownMenu(
                                expanded = classMenuExpanded,
                                onDismissRequest = { classMenuExpanded = false }
                            ) {
                                commonClasses.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text(cls, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            gradeClass = cls
                                            classMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Batch Dropdown
                        ExposedDropdownMenuBox(
                            expanded = batchMenuExpanded,
                            onExpandedChange = { batchMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = batch,
                                onValueChange = { batch = it },
                                label = { Text("Assigned Batch / Schedule *") },
                                leadingIcon = {
                                    Icon(Icons.Default.Schedule, contentDescription = null)
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            )
                            ExposedDropdownMenu(
                                expanded = batchMenuExpanded,
                                onDismissRequest = { batchMenuExpanded = false }
                            ) {
                                if (batches.isNotEmpty()) {
                                    batches.forEach { b ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = b.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = "${b.gradeClass} • ${b.timeSlot}",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                batch = if (b.timeSlot.isNotBlank()) "${b.name} (${b.timeSlot})" else b.name
                                                gradeClass = b.gradeClass
                                                if (b.feeAmount > 0 && (monthlyFee == "1500" || monthlyFee.isBlank())) {
                                                    monthlyFee = b.feeAmount.toInt().toString()
                                                }
                                                batchMenuExpanded = false
                                            }
                                        )
                                    }
                                } else {
                                    timingPresets.forEach { time ->
                                        DropdownMenuItem(
                                            text = { Text("Class 10 ($time)") },
                                            onClick = {
                                                batch = "Batch ($time)"
                                                batchMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Flexible Timing presets row (12 PM - 8 PM)
                        Text(
                            text = "Quick Timing Presets (12:00 PM – 8:00 PM):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            timingPresets.take(6).forEach { preset ->
                                FilterChip(
                                    selected = batch.contains(preset),
                                    onClick = {
                                        batch = if (batch.contains("(")) {
                                            batch.substringBefore("(") + "($preset)"
                                        } else {
                                            "$batch ($preset)"
                                        }
                                    },
                                    label = { Text(preset, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 3: Fee Structure
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monthly Fee Schedule",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = monthlyFee,
                                onValueChange = {
                                    monthlyFee = it
                                    feeError = it.toDoubleOrNull() == null
                                },
                                label = { Text("Monthly Fee (₹) *") },
                                isError = feeError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("student_fee_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = dueDay,
                                onValueChange = { dueDay = it },
                                label = { Text("Due Day (1-28)") },
                                placeholder = { Text("5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Quick Due Date presets:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("1", "5", "10", "15", "25").forEach { day ->
                                FilterChip(
                                    selected = dueDay == day,
                                    onClick = { dueDay = day },
                                    label = { Text("${day}th of month") }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 4: Contact & Notices
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Contact & WhatsApp Notifications",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneError = it.isBlank()
                            },
                            label = { Text("Student Contact / WhatsApp *") },
                            placeholder = { Text("e.g. 9876543210") },
                            isError = phoneError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_phone_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = parentPhone,
                            onValueChange = { parentPhone = it },
                            label = { Text("Parent / Guardian Phone") },
                            placeholder = { Text("For fee reminder notices") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = admissionDate,
                            onValueChange = { admissionDate = it },
                            label = { Text("Admission Date (YYYY-MM-DD)") },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Remarks / Special Notes") },
                            placeholder = { Text("e.g. Sibling discount, target exams...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Big Bottom Action Buttons with Generous Navigation Bar Inset Protection
                // Positioned safely away from the phone's bottom hardware/gesture back buttons
                Button(
                    onClick = { submitForm() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_student_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (studentToEdit == null) Icons.Default.PersonAdd else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (studentToEdit == null) "Add Student" else "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }

                // Generous bottom spacer plus device system navigation bar padding to prevent any accidental back-button taps
                Spacer(modifier = Modifier.height(36.dp + navBarPadding.calculateBottomPadding()))
            }
        }
    }
}
