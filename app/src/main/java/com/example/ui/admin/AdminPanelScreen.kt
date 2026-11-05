package com.example.ui.admin

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.*
import com.example.ui.components.FilterChipRow
import com.example.ui.components.StatCard
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.state.SchoolViewModel
import com.example.ui.theme.*

@Composable
fun AdminPanelScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val exams by viewModel.allExams.collectAsStateWithLifecycle()
    val notices by viewModel.allNotices.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Dashboard/Students, 1: Teachers, 2: Exams & Results, 3: Timetable, 4: Notices
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showAddNoticeDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var selectedStudentForDetail by remember { mutableStateOf<StudentEntity?>(null) }

    val selectedClass by viewModel.selectedClassFilter.collectAsStateWithLifecycle()
    val selectedSection by viewModel.selectedSectionFilter.collectAsStateWithLifecycle()
    val selectedGender by viewModel.selectedGenderFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Filtered students for admin view
    val filteredStudents = remember(students, selectedClass, selectedSection, selectedGender, searchQuery) {
        students.filter { student ->
            val matchClass = student.classNumber == selectedClass
            val matchSection = selectedSection == "All" || student.section == selectedSection
            val matchGender = selectedGender == "All" || student.gender == selectedGender
            val matchQuery = searchQuery.isBlank() ||
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.admissionNo.contains(searchQuery, ignoreCase = true) ||
                    student.rollNumber.toString() == searchQuery
            matchClass && matchSection && matchGender && matchQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MicdBackground)
    ) {
        // Banner card with school info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_micd_campus),
                    contentDescription = "M.I.C.D Campus",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color(0xE60D47A1), Color(0x990D47A1))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "एम.आई.सी.डी स्कूल प्रशासन केंद्र" else "M.I.C.D School Administration",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "सत्र 2024-2025 • कक्षा 1 से 12 तक पूर्ण नियंत्रण" else "Academic Session 2024-2025 • Full Control (Classes 1-12)",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    FilledTonalButton(
                        onClick = { activeTab = 5 },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MicdGold,
                            contentColor = Color(0xFF1A2332)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("admin_fcm_quick_btn")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "FCM अलर्ट" else "FCM Console",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Sub Navigation Tabs
        val tabs = listOf(
            AppStrings.students(currentLang),
            AppStrings.teachers(currentLang),
            AppStrings.examinations(currentLang),
            AppStrings.timetable(currentLang),
            AppStrings.notices(currentLang),
            if (currentLang == AppLanguage.HINDI) "पुश सूचनाएं (FCM)" else "FCM Push Alerts"
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

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area per Sub-Tab
        when (activeTab) {
            0 -> {
                // Students Management Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Stat Overview Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = if (currentLang == AppLanguage.HINDI) "कुल छात्र" else "Total Students",
                            value = "${students.size}",
                            icon = Icons.Default.Groups,
                            accentColor = MicdPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        val boysCount = students.count { it.gender == "Boy" }
                        StatCard(
                            title = if (currentLang == AppLanguage.HINDI) "छात्र (लड़के)" else "Boys",
                            value = "$boysCount",
                            icon = Icons.Default.Male,
                            accentColor = Color(0xFF1976D2),
                            modifier = Modifier.weight(1f)
                        )
                        val girlsCount = students.count { it.gender == "Girl" }
                        StatCard(
                            title = if (currentLang == AppLanguage.HINDI) "छात्राएं (लड़कियां)" else "Girls",
                            value = "$girlsCount",
                            icon = Icons.Default.Female,
                            accentColor = Color(0xFFE91E63),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filters (Classes 1-12, Section A/B/C, Gender Boys/Girls)
                    FilterChipRow(
                        selectedClass = selectedClass,
                        onClassSelected = { viewModel.selectedClassFilter.value = it },
                        selectedSection = selectedSection,
                        onSectionSelected = { viewModel.selectedSectionFilter.value = it },
                        selectedGender = selectedGender,
                        onGenderSelected = { viewModel.selectedGenderFilter.value = it },
                        currentLang = currentLang
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search & Add Student button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text(if (currentLang == AppLanguage.HINDI) "नाम या प्रवेश संख्या खोजें..." else "Search by name or admission no...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MicdTextSecondary) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showAddStudentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("add_student_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLang == AppLanguage.HINDI) "+ नया छात्र" else "+ Add Student", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Students List
                    Text(
                        text = if (currentLang == AppLanguage.HINDI)
                            "कक्षा $selectedClass-$selectedSection छात्र सूची (${filteredStudents.size})"
                        else
                            "Class $selectedClass-$selectedSection Students (${filteredStudents.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MicdTextPrimary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredStudents, key = { it.id }) { student ->
                            StudentListItem(
                                student = student,
                                currentLang = currentLang,
                                onClick = { selectedStudentForDetail = student },
                                onDelete = { viewModel.deleteStudent(student.id) }
                            )
                        }
                    }
                }
            }

            1 -> {
                // Teachers Tab
                TeachersManagementView(
                    teachers = teachers,
                    currentLang = currentLang,
                    onAddTeacher = { showAddTeacherDialog = true },
                    onDeleteTeacher = { viewModel.deleteTeacher(it) }
                )
            }

            2 -> {
                // Examinations & Results Publishing Tab
                ExamsManagementView(
                    exams = exams,
                    currentLang = currentLang,
                    onTogglePublished = { viewModel.toggleExamPublished(it) },
                    onAddExam = { showAddExamDialog = true },
                    onSelectExam = { viewModel.refreshRankings(it) }
                )
            }

            3 -> {
                // Timetable View
                AdminTimetableView(
                    viewModel = viewModel,
                    currentLang = currentLang
                )
            }

            4 -> {
                // Notices Management Tab
                NoticesManagementView(
                    notices = notices,
                    currentLang = currentLang,
                    onAddNotice = { showAddNoticeDialog = true },
                    onDeleteNotice = { viewModel.deleteNotice(it) }
                )
            }

            5 -> {
                // FCM Push Notification Management & Diagnostics Tab
                com.example.ui.notifications.AdminNotificationPanelScreen(
                    viewModel = viewModel,
                    onBack = { activeTab = 0 },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Add Student Dialog
    if (showAddStudentDialog) {
        AddStudentDialog(
            defaultClass = selectedClass,
            defaultSection = selectedSection,
            currentLang = currentLang,
            onDismiss = { showAddStudentDialog = false },
            onAdd = { newStudent ->
                viewModel.addStudent(newStudent)
                showAddStudentDialog = false
            }
        )
    }

    // Add Teacher Dialog
    if (showAddTeacherDialog) {
        AddTeacherDialog(
            currentLang = currentLang,
            onDismiss = { showAddTeacherDialog = false },
            onAdd = { newTeacher ->
                viewModel.addTeacher(newTeacher)
                showAddTeacherDialog = false
            }
        )
    }

    // Add Notice Dialog
    if (showAddNoticeDialog) {
        AddNoticeDialog(
            currentLang = currentLang,
            onDismiss = { showAddNoticeDialog = false },
            onAdd = { notice ->
                viewModel.postNotice(notice)
                showAddNoticeDialog = false
            }
        )
    }

    // Student Detail Dialog
    selectedStudentForDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            currentLang = currentLang,
            onDismiss = { selectedStudentForDetail = null }
        )
    }
}

@Composable
fun StudentListItem(
    student: StudentEntity,
    currentLang: AppLanguage,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.rollNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Roll Number Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (student.gender == "Girl") Color(0xFFFCE4EC) else Color(0xFFE3F2FD),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${student.rollNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (student.gender == "Girl") Color(0xFFC2185B) else MicdPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MicdTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (student.gender == "Girl") Color(0xFFE91E63).copy(alpha = 0.12f) else Color(0xFF1976D2).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (student.gender == "Girl") "Girl" else "Boy",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (student.gender == "Girl") Color(0xFFE91E63) else Color(0xFF1976D2),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Adm: ${student.admissionNo} • Class ${student.classNumber}-${student.section}",
                    fontSize = 11.sp,
                    color = MicdTextSecondary
                )
                Text(
                    text = "Father: ${student.fatherName}",
                    fontSize = 11.sp,
                    color = MicdTextMuted
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MicdError.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TeachersManagementView(
    teachers: List<TeacherEntity>,
    currentLang: AppLanguage,
    onAddTeacher: () -> Unit,
    onDeleteTeacher: (Long) -> Unit
) {
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
                text = if (currentLang == AppLanguage.HINDI) "संकाय एवं शिक्षक सूची (${teachers.size})" else "Faculty & Teachers (${teachers.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MicdTextPrimary
            )
            Button(
                onClick = onAddTeacher,
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (currentLang == AppLanguage.HINDI) "+ शिक्षक जोड़ें" else "+ Add Teacher", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(teachers, key = { it.id }) { teacher ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MicdPrimary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CastForEducation, contentDescription = null, tint = MicdPrimary)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = teacher.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MicdTextPrimary
                            )
                            Text(
                                text = "ID: ${teacher.teacherId} • ${teacher.qualification}",
                                fontSize = 11.sp,
                                color = MicdTextSecondary
                            )
                            Text(
                                text = "Subjects: ${teacher.assignedSubjects} • Classes: ${teacher.assignedClasses}",
                                fontSize = 11.sp,
                                color = MicdPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (teacher.isClassTeacherOf.isNotBlank()) {
                                Text(
                                    text = "Class Teacher: ${teacher.isClassTeacherOf}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteTeacher(teacher.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MicdError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamsManagementView(
    exams: List<ExamEntity>,
    currentLang: AppLanguage,
    onTogglePublished: (ExamEntity) -> Unit,
    onAddExam: () -> Unit,
    onSelectExam: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = if (currentLang == AppLanguage.HINDI) "परीक्षाएं एवं परिणाम प्रकाशन" else "Examinations & Result Publication",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MicdTextPrimary
        )
        Text(
            text = if (currentLang == AppLanguage.HINDI)
                "परिणाम प्रकाशित करने पर छात्र और अभिभावक अपने अंक और रैंक कार्ड देख सकते हैं।"
            else
                "When an exam is published, students and parents can instantly view report cards and rankings.",
            fontSize = 11.sp,
            color = MicdTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(exams, key = { it.id }) { exam ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectExam(exam.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exam.examName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MicdTextPrimary
                                )
                                Text(
                                    text = "Type: ${exam.examType} • Session: ${exam.academicYear}",
                                    fontSize = 12.sp,
                                    color = MicdTextSecondary
                                )
                                Text(
                                    text = "Schedule: ${exam.startDate} to ${exam.endDate}",
                                    fontSize = 11.sp,
                                    color = MicdTextMuted
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (exam.isPublished) Color(0xFF2E7D32).copy(alpha = 0.12f) else Color(0xFFED6C02).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (exam.isPublished) "PUBLISHED" else "DRAFT",
                                        color = if (exam.isPublished) Color(0xFF2E7D32) else Color(0xFFED6C02),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = { onTogglePublished(exam) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (exam.isPublished) Color(0xFFED6C02) else Color(0xFF2E7D32)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (exam.isPublished) "Unpublish" else "Publish",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTimetableView(
    viewModel: SchoolViewModel,
    currentLang: AppLanguage
) {
    val selectedClass by viewModel.selectedClassFilter.collectAsStateWithLifecycle()
    val selectedSection by viewModel.selectedSectionFilter.collectAsStateWithLifecycle()
    val timetable by viewModel.getAttendanceByClassDate(selectedClass, selectedSection, "").collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Class $selectedClass-$selectedSection Timetable (Monday to Friday)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MicdTextPrimary
        )
        Text(
            text = "7 Daily Periods: 08:00 AM to 02:15 PM",
            fontSize = 11.sp,
            color = MicdTextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        val periodSlots = listOf(
            Triple(1, "08:00 - 08:45 AM", "Mathematics (Mr. Rajesh Sharma)"),
            Triple(2, "08:45 - 09:30 AM", "Science (Mrs. Sunita Verma)"),
            Triple(3, "09:30 - 10:15 AM", "English (Mrs. Priya Nair)"),
            Triple(4, "10:45 - 11:30 AM", "Hindi (Mr. Vikramaditya Singh)"),
            Triple(5, "11:30 - 12:15 PM", "Social Science (Dr. Anand Joshi)"),
            Triple(6, "12:15 - 01:00 PM", "Computer Science (Mrs. Neha Gupta)"),
            Triple(7, "01:30 - 02:15 PM", "Physical Education / Sports")
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(periodSlots) { (num, time, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                .background(MicdPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("P$num", fontWeight = FontWeight.Bold, color = MicdPrimary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(desc, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MicdTextPrimary)
                            Text(time, fontSize = 11.sp, color = MicdTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoticesManagementView(
    notices: List<NoticeEntity>,
    currentLang: AppLanguage,
    onAddNotice: () -> Unit,
    onDeleteNotice: (Long) -> Unit
) {
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
                text = if (currentLang == AppLanguage.HINDI) "आधिकारिक सूचना पट्ट" else "Official Notice Board",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MicdTextPrimary
            )
            Button(
                onClick = onAddNotice,
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (currentLang == AppLanguage.HINDI) "+ सूचना जारी करें" else "+ Publish Notice", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(notices, key = { it.id }) { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (notice.isUrgent) MicdError.copy(alpha = 0.12f) else MicdSecondary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = notice.category.uppercase(),
                                    color = if (notice.isUrgent) MicdError else MicdSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = notice.date,
                                fontSize = 11.sp,
                                color = MicdTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = notice.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MicdTextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = notice.content,
                            fontSize = 12.sp,
                            color = MicdTextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Issued by: ${notice.author}",
                                fontSize = 11.sp,
                                color = MicdTextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(onClick = { onDeleteNotice(notice.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MicdError, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialogs
@Composable
fun AddStudentDialog(
    defaultClass: Int,
    defaultSection: String,
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onAdd: (StudentEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("2010-05-15") }
    var gender by remember { mutableStateOf("Boy") }
    var classNum by remember { mutableStateOf(defaultClass) }
    var section by remember { mutableStateOf(defaultSection) }
    var rollNo by remember { mutableStateOf("15") }
    var contact by remember { mutableStateOf("+91 98765 00000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (currentLang == AppLanguage.HINDI) "नया छात्र पंजीकृत करें" else "Register New Student", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Student Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = gender == "Boy",
                            onClick = { gender = "Boy" },
                            label = { Text("Boy") }
                        )
                        FilterChip(
                            selected = gender == "Girl",
                            onClick = { gender = "Girl" },
                            label = { Text("Girl") }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("Father's Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = motherName,
                        onValueChange = { motherName = it },
                        label = { Text("Mother's Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = classNum.toString(),
                            onValueChange = { classNum = it.toIntOrNull() ?: classNum },
                            label = { Text("Class (1-12)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = section,
                            onValueChange = { section = it.uppercase() },
                            label = { Text("Section") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rollNo,
                            onValueChange = { rollNo = it },
                            label = { Text("Roll (1-100)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text("Contact Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val rollInt = rollNo.toIntOrNull() ?: 1
                        onAdd(
                            StudentEntity(
                                studentId = "MICD-$classNum$section-${rollInt.toString().padStart(3, '0')}",
                                admissionNo = "ADM-2024-${System.currentTimeMillis().toString().takeLast(4)}",
                                name = name,
                                fatherName = fatherName.ifBlank { "Parent" },
                                motherName = motherName.ifBlank { "Mother" },
                                dob = dob,
                                gender = gender,
                                classNumber = classNum.coerceIn(1, 12),
                                section = section.ifBlank { "A" },
                                rollNumber = rollInt.coerceIn(1, 100),
                                contactNumber = contact
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
            ) {
                Text("Save Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTeacherDialog(
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onAdd: (TeacherEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("M.Sc., B.Ed.") }
    var assignedSubjects by remember { mutableStateOf("Mathematics") }
    var assignedClasses by remember { mutableStateOf("9,10") }
    var phone by remember { mutableStateOf("+91 98765 00000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Faculty Member", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Teacher Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = { Text("Qualification") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = assignedSubjects,
                    onValueChange = { assignedSubjects = it },
                    label = { Text("Assigned Subjects (e.g. Science, Physics)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = assignedClasses,
                    onValueChange = { assignedClasses = it },
                    label = { Text("Assigned Classes (e.g. 8,9,10)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            TeacherEntity(
                                teacherId = "TCH-${System.currentTimeMillis().toString().takeLast(3)}",
                                name = name,
                                email = "${name.lowercase().replace(" ", ".")}@micdschool.edu",
                                phone = phone,
                                qualification = qualification,
                                assignedClasses = assignedClasses,
                                assignedSubjects = assignedSubjects
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
            ) {
                Text("Save Teacher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddNoticeDialog(
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onAdd: (NoticeEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var isUrgent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish School Notice", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                    Text("Mark as Urgent Alert")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onAdd(
                            NoticeEntity(
                                title = title,
                                content = content,
                                category = category,
                                date = "2024-11-04",
                                isUrgent = isUrgent
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
            ) {
                Text("Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StudentDetailDialog(
    student: StudentEntity,
    currentLang: AppLanguage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = MicdPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(student.name, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Admission No: ${student.admissionNo}", fontWeight = FontWeight.SemiBold)
                Text("Student ID: ${student.studentId}")
                Text("Class & Section: Class ${student.classNumber}-${student.section}")
                Text("Roll Number: ${student.rollNumber}")
                Text("Gender: ${student.gender}")
                Text("Father's Name: ${student.fatherName}")
                Text("Mother's Name: ${student.motherName}")
                Text("Date of Birth: ${student.dob}")
                Text("Contact: ${student.contactNumber}")
                Text("Email: ${student.email}")
                Text("Address: ${student.address}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)) {
                Text("Close")
            }
        }
    )
}
