package com.example.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.StatCard
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.state.SchoolViewModel
import com.example.ui.theme.*

@Composable
fun TeacherPanelScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val selectedTeacher by viewModel.selectedTeacher.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val homeworkList by viewModel.allHomework.collectAsStateWithLifecycle()
    val notesList by viewModel.allNotes.collectAsStateWithLifecycle()
    val exams by viewModel.allExams.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Attendance, 1: Homework, 2: Notes & AI Gen, 3: Exam Marks
    var selectedClass by remember { mutableStateOf(10) }
    var selectedSection by remember { mutableStateOf("A") }
    var selectedDate by remember { mutableStateOf("2024-11-04") }

    // Dialogs
    var showUploadHomeworkDialog by remember { mutableStateOf(false) }
    var showUploadNoteDialog by remember { mutableStateOf(false) }
    var showAiNotesGeneratorDialog by remember { mutableStateOf(false) }
    var showEnterMarksDialog by remember { mutableStateOf(false) }
    var selectedStudentForMarks by remember { mutableStateOf<StudentEntity?>(null) }

    // Attendance state map for the current class & section
    val classStudents = remember(students, selectedClass, selectedSection) {
        students.filter { it.classNumber == selectedClass && it.section == selectedSection }
    }

    var attendanceStateMap by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    // Initialize attendance status when students list changes
    LaunchedEffect(classStudents) {
        if (attendanceStateMap.isEmpty() || !attendanceStateMap.keys.containsAll(classStudents.map { it.id })) {
            attendanceStateMap = classStudents.associate { it.id to "Present" }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MicdBackground)
    ) {
        // Teacher Profile Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MicdSecondary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = MicdSecondary, modifier = Modifier.size(26.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedTeacher?.name ?: "Faculty Member",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MicdTextPrimary
                        )
                        Text(
                            text = "Assigned Subjects: ${selectedTeacher?.assignedSubjects ?: "Mathematics, Science"}",
                            fontSize = 12.sp,
                            color = MicdSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Classes: ${selectedTeacher?.assignedClasses ?: "9, 10"} • Teacher ID: ${selectedTeacher?.teacherId ?: "TCH-001"}",
                            fontSize = 11.sp,
                            color = MicdTextSecondary
                        )
                    }

                    // Quick Switch Teacher dropdown
                    if (teachers.size > 1) {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Teacher", tint = MicdPrimary)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                teachers.forEach { tch ->
                                    DropdownMenuItem(
                                        text = { Text("${tch.name} (${tch.assignedSubjects})") },
                                        onClick = {
                                            viewModel.selectTeacher(tch)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sub Tabs
        val tabs = listOf(
            AppStrings.attendance(currentLang),
            AppStrings.homework(currentLang),
            AppStrings.studyNotes(currentLang) + " & AI",
            if (currentLang == AppLanguage.HINDI) "परीक्षा अंक" else "Exam Marks"
        )

        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = activeTab == index
                Tab(
                    selected = isSelected,
                    onClick = { activeTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MicdPrimary else MicdTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        when (activeTab) {
            0 -> {
                // Attendance Marking Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Class & Section Selector for Attendance
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(8, 9, 10, 11, 12).forEach { c ->
                                        FilterChip(
                                            selected = selectedClass == c,
                                            onClick = { selectedClass = c },
                                            label = { Text("Class $c", fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("A", "B").forEach { sec ->
                                        FilterChip(
                                            selected = selectedSection == sec,
                                            onClick = { selectedSection = sec },
                                            label = { Text("Sec $sec", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date: $selectedDate",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MicdTextPrimary
                                )

                                Button(
                                    onClick = {
                                        // Mark all present
                                        attendanceStateMap = classStudents.associate { it.id to "Present" }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MicdSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (currentLang == AppLanguage.HINDI) "सभी उपस्थित" else "Mark All Present", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Students (${classStudents.size}) - Tap status to change",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MicdTextPrimary
                        )

                        Button(
                            onClick = {
                                viewModel.saveAttendance(selectedClass, selectedSection, selectedDate, attendanceStateMap)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_attendance_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLang == AppLanguage.HINDI) "उपस्थिति सहेजें" else "Save Attendance", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(classStudents, key = { it.id }) { student ->
                            val status = attendanceStateMap[student.id] ?: "Present"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Roll ${student.rollNumber}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MicdPrimary,
                                        modifier = Modifier.width(60.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MicdTextPrimary
                                        )
                                        Text(
                                            text = "Adm: ${student.admissionNo} • ${student.gender}",
                                            fontSize = 10.sp,
                                            color = MicdTextMuted
                                        )
                                    }

                                    // Status Button (cycles Present -> Absent -> Leave -> Present)
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val next = when (status) {
                                                    "Present" -> "Absent"
                                                    "Absent" -> "Leave"
                                                    else -> "Present"
                                                }
                                                attendanceStateMap = attendanceStateMap + (student.id to next)
                                            },
                                        color = when (status) {
                                            "Present" -> Color(0xFF2E7D32)
                                            "Absent" -> Color(0xFFD32F2F)
                                            else -> Color(0xFFED6C02)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = status.uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Homework Management Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "अपलोड किया गया गृहकार्य" else "Uploaded Homework",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MicdTextPrimary
                        )
                        Button(
                            onClick = { showUploadHomeworkDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("upload_homework_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLang == AppLanguage.HINDI) "+ गृहकार्य अपलोड" else "+ Upload Homework", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(homeworkList, key = { it.id }) { hw ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MicdPrimary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "${hw.subject} • Class ${hw.classNumber}-${hw.section}",
                                                color = MicdPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                        Text(
                                            text = "Due: ${hw.dueDate}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFD32F2F),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = hw.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MicdTextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = hw.description,
                                        fontSize = 12.sp,
                                        color = MicdTextSecondary
                                    )

                                    if (hw.attachmentContent.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Attachment,
                                                contentDescription = null,
                                                tint = MicdSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Attachment [${hw.attachmentType}]: ${hw.attachmentContent}",
                                                fontSize = 11.sp,
                                                color = MicdSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "By ${hw.teacherName} on ${hw.assignedDate}",
                                            fontSize = 10.sp,
                                            color = MicdTextMuted
                                        )
                                        IconButton(onClick = { viewModel.deleteHomework(hw.id) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MicdError, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Study Notes & AI Notes Generator Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // AI Generator Promo Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MicdPrimary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "एआई नोट्स एवं वर्कशीट जनरेटर" else "AI Notes & Worksheet Generator",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MicdTextPrimary
                                )
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "कक्षा 1-12 के लिए अध्याय सारांश, MCQs व प्रश्न पत्र तुरंत तैयार करें" else "Generate chapter summaries, MCQs, and question banks instantly",
                                    fontSize = 11.sp,
                                    color = MicdTextSecondary
                                )
                            }
                            Button(
                                onClick = { showAiNotesGeneratorDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("ai_notes_generator_btn")
                            ) {
                                Text("Generate", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "अध्ययन नोट्स लाइब्रेरी (${notesList.size})" else "Study Notes Library (${notesList.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MicdTextPrimary
                        )
                        Button(
                            onClick = { showUploadNoteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MicdSecondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Manual Note", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(notesList, key = { it.id }) { note ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MicdPrimary.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = "Class ${note.classNumber} • ${note.subject} • Ch ${note.chapterNumber}",
                                                color = MicdPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(note.dateAdded, fontSize = 11.sp, color = MicdTextMuted)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = note.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MicdTextPrimary
                                    )
                                    Text(
                                        text = "Topic: ${note.topic}",
                                        fontSize = 12.sp,
                                        color = MicdSecondary,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = note.content.take(160) + if (note.content.length > 160) "..." else "",
                                        fontSize = 11.sp,
                                        color = MicdTextSecondary,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("By ${note.authorName}", fontSize = 10.sp, color = MicdTextMuted)
                                        IconButton(onClick = { viewModel.deleteStudyNote(note.id) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MicdError, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Exam Marks Entry Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "छात्र परीक्षा अंक प्रविष्टि" else "Student Examination Marks Entry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MicdTextPrimary
                    )
                    Text(
                        text = "Enter and update subject marks out of 100 before publication.",
                        fontSize = 11.sp,
                        color = MicdTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(classStudents, key = { it.id }) { student ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedStudentForMarks = student
                                        showEnterMarksDialog = true
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MicdPrimary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("#${student.rollNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MicdPrimary)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MicdTextPrimary)
                                        Text("Class ${student.classNumber}-${student.section} • Adm: ${student.admissionNo}", fontSize = 11.sp, color = MicdTextSecondary)
                                    }

                                    Button(
                                        onClick = {
                                            selectedStudentForMarks = student
                                            showEnterMarksDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Enter Marks", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Upload Homework Dialog
    if (showUploadHomeworkDialog) {
        UploadHomeworkDialog(
            teacherName = selectedTeacher?.name ?: "Faculty",
            currentLang = currentLang,
            onDismiss = { showUploadHomeworkDialog = false },
            onUpload = { hw ->
                viewModel.addHomework(hw)
                showUploadHomeworkDialog = false
            }
        )
    }

    // Upload Study Note Dialog
    if (showUploadNoteDialog) {
        UploadNoteDialog(
            teacherName = selectedTeacher?.name ?: "Faculty",
            currentLang = currentLang,
            onDismiss = { showUploadNoteDialog = false },
            onUpload = { note ->
                viewModel.addStudyNote(note)
                showUploadNoteDialog = false
            }
        )
    }

    // AI Notes Generator Dialog
    if (showAiNotesGeneratorDialog) {
        AiNotesGeneratorDialog(
            viewModel = viewModel,
            currentLang = currentLang,
            onDismiss = { showAiNotesGeneratorDialog = false }
        )
    }

    // Enter Marks Dialog
    if (showEnterMarksDialog && selectedStudentForMarks != null) {
        EnterMarksDialog(
            student = selectedStudentForMarks!!,
            examId = exams.firstOrNull()?.id ?: 1L,
            subject = selectedTeacher?.assignedSubjects?.split(",")?.firstOrNull() ?: "Mathematics",
            onDismiss = { showEnterMarksDialog = false },
            onSave = { mark ->
                viewModel.saveExamMark(mark)
                showEnterMarksDialog = false
            }
        )
    }
}

@Composable
fun UploadHomeworkDialog(
    teacherName: String,
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onUpload: (HomeworkEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var classNum by remember { mutableStateOf(10) }
    var section by remember { mutableStateOf("A") }
    var subject by remember { mutableStateOf("Mathematics") }
    var dueDate by remember { mutableStateOf("2024-11-09") }
    var attachmentType by remember { mutableStateOf("PDF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Class Homework", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Homework Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = classNum.toString(),
                        onValueChange = { classNum = it.toIntOrNull() ?: classNum },
                        label = { Text("Class (1-12)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions & Exercises") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onUpload(
                            HomeworkEntity(
                                title = title,
                                description = description,
                                classNumber = classNum,
                                section = section,
                                subject = subject,
                                teacherName = teacherName,
                                assignedDate = "2024-11-04",
                                dueDate = dueDate,
                                attachmentType = attachmentType,
                                attachmentContent = "$title.pdf"
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UploadNoteDialog(
    teacherName: String,
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onUpload: (StudyNoteEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Science") }
    var classNum by remember { mutableStateOf(10) }
    var chapterName by remember { mutableStateOf("Electricity and Magnetism") }
    var topic by remember { mutableStateOf("Ohm's Law and Circuit Diagrams") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Study Notes", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Notes Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = classNum.toString(),
                            onValueChange = { classNum = it.toIntOrNull() ?: classNum },
                            label = { Text("Class") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = chapterName,
                        onValueChange = { chapterName = it },
                        label = { Text("Chapter Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Note Content & Formulas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onUpload(
                            StudyNoteEntity(
                                title = title,
                                classNumber = classNum,
                                subject = subject,
                                chapterNumber = 1,
                                chapterName = chapterName,
                                topic = topic,
                                content = content,
                                authorName = teacherName,
                                isApproved = true,
                                dateAdded = "2024-11-04"
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
            ) {
                Text("Publish Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AiNotesGeneratorDialog(
    viewModel: SchoolViewModel,
    currentLang: AppLanguage,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf("Science") }
    var classLevel by remember { mutableStateOf(10) }
    var chapter by remember { mutableStateOf("Carbon and its Compounds") }

    val isThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val generatedResource by viewModel.generatedResource.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MicdPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "एम.आई.सी.डी एआई नोट्स जनरेटर" else "M.I.C.D AI Notes Generator",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (isThinking) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MicdPrimary)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Generating comprehensive study material...", fontSize = 13.sp, color = MicdTextPrimary)
                    Text("Creating chapter summaries, key points & MCQs", fontSize = 11.sp, color = MicdTextSecondary)
                }
            } else if (generatedResource != null) {
                val res = generatedResource!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(res.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MicdPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(res.summary, fontSize = 12.sp, color = MicdTextSecondary)
                    }
                    item {
                        Text("Key Learning Points:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        res.keyPoints.forEach { pt ->
                            Text("• $pt", fontSize = 11.sp, color = MicdTextPrimary)
                        }
                    }
                    item {
                        Text("Generated MCQs (${res.mcqs.size}):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        res.mcqs.forEachIndexed { i, mcq ->
                            Text("${i + 1}. ${mcq.question}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("Answer: ${mcq.options.getOrNull(mcq.correctIndex)}", fontSize = 10.sp, color = Color(0xFF2E7D32))
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Specify subject and chapter to create teacher-approved study materials:",
                        fontSize = 12.sp,
                        color = MicdTextSecondary
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (e.g. Science, Mathematics)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = classLevel.toString(),
                        onValueChange = { classLevel = it.toIntOrNull() ?: classLevel },
                        label = { Text("Target Class (1-12)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("Chapter Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (generatedResource != null) {
                Button(
                    onClick = {
                        viewModel.publishGeneratedNoteAsStudyMaterial(generatedResource!!, classLevel, subject)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Approve & Publish to School")
                }
            } else if (!isThinking) {
                Button(
                    onClick = {
                        viewModel.generateAiNotes(subject, classLevel, chapter)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
                ) {
                    Text("Generate Material")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.clearGeneratedResource()
                onDismiss()
            }) {
                Text("Close")
            }
        }
    )
}

@Composable
fun EnterMarksDialog(
    student: StudentEntity,
    examId: Long,
    subject: String,
    onDismiss: () -> Unit,
    onSave: (ExamMarkEntity) -> Unit
) {
    var marksObtained by remember { mutableStateOf("88") }
    var maxMarks by remember { mutableStateOf("100") }
    var remarks by remember { mutableStateOf("Outstanding analytical work") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Marks: ${student.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Subject: $subject • Roll: #${student.rollNumber} • Class ${student.classNumber}-${student.section}")
                OutlinedTextField(
                    value = marksObtained,
                    onValueChange = { marksObtained = it },
                    label = { Text("Marks Obtained") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxMarks,
                    onValueChange = { maxMarks = it },
                    label = { Text("Maximum Marks") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Teacher Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marksInt = marksObtained.toIntOrNull() ?: 0
                    val maxInt = maxMarks.toIntOrNull() ?: 100
                    onSave(
                        ExamMarkEntity(
                            examId = examId,
                            studentId = student.id,
                            subject = subject,
                            maxMarks = maxInt,
                            marksObtained = marksInt,
                            teacherRemarks = remarks
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
            ) {
                Text("Save Marks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
