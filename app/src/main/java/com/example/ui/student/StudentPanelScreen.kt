package com.example.ui.student

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
import com.example.data.repository.StudentExamResult
import com.example.ui.components.StatCard
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.state.SchoolViewModel
import com.example.ui.theme.*

@Composable
fun StudentPanelScreen(
    viewModel: SchoolViewModel,
    onNavigateToAi: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToVideos: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val homeworkList by viewModel.allHomework.collectAsStateWithLifecycle()
    val notices by viewModel.allNotices.collectAsStateWithLifecycle()
    val exams by viewModel.allExams.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Overview, 1: Result/Report Card, 2: Homework, 3: Attendance

    val student = selectedStudent ?: students.firstOrNull()

    // Student specific attendance
    val studentAttendance by (if (student != null) viewModel.getAttendanceForStudent(student.id) else kotlinx.coroutines.flow.flowOf(emptyList<AttendanceEntity>()))
        .collectAsStateWithLifecycle(emptyList())

    val attendanceSummary = remember(studentAttendance) {
        viewModel.getAttendanceSummary(studentAttendance)
    }

    // Student specific homework
    val studentHomework = remember(homeworkList, student) {
        if (student == null) emptyList()
        else homeworkList.filter { it.classNumber == student.classNumber && (it.section == student.section || it.section == "All") }
    }

    // Student exam result
    var examResult by remember { mutableStateOf<StudentExamResult?>(null) }
    LaunchedEffect(student, exams) {
        if (student != null && exams.isNotEmpty()) {
            val publishedExam = exams.firstOrNull { it.isPublished } ?: exams.first()
            examResult = viewModel.getStudentResult(publishedExam.id, student.id)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MicdBackground)
    ) {
        // Student Profile Header Card
        student?.let { stu ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(
                                    if (stu.gender == "Girl") Color(0xFFFCE4EC) else Color(0xFFE3F2FD),
                                    CircleShape
                                )
                                .border(
                                    2.dp,
                                    if (stu.gender == "Girl") Color(0xFFE91E63) else MicdPrimary,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stu.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (stu.gender == "Girl") Color(0xFFC2185B) else MicdPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stu.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MicdTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MicdPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Roll #${stu.rollNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MicdPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Class ${stu.classNumber}-${stu.section} • Adm No: ${stu.admissionNo}",
                                fontSize = 12.sp,
                                color = MicdTextSecondary
                            )
                            Text(
                                text = "Student ID: ${stu.studentId} • ${stu.gender}",
                                fontSize = 11.sp,
                                color = MicdTextMuted
                            )
                        }

                        // Profile chooser dropdown for testing
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.SwitchAccount, contentDescription = "Switch Student", tint = MicdPrimary)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                students.take(15).forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text("${s.name} (Class ${s.classNumber}-${s.section} Roll ${s.rollNumber})") },
                                        onClick = {
                                            viewModel.selectStudent(s)
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
            AppStrings.dashboard(currentLang),
            AppStrings.results(currentLang),
            AppStrings.homework(currentLang),
            AppStrings.attendance(currentLang)
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

        // Content
        when (activeTab) {
            0 -> {
                // Overview Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Quick Stat Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = AppStrings.attendance(currentLang),
                                value = String.format("%.1f%%", attendanceSummary.percentage),
                                icon = Icons.Default.EventAvailable,
                                accentColor = if (attendanceSummary.percentage >= 75.0) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                modifier = Modifier.weight(1f),
                                subtitle = "${attendanceSummary.presentDays} of ${attendanceSummary.totalDays} Days"
                            )
                            StatCard(
                                title = if (currentLang == AppLanguage.HINDI) "कक्षा रैंक" else "Class Rank",
                                value = if (examResult != null) "#${examResult!!.classRank}" else "#1",
                                icon = Icons.Default.EmojiEvents,
                                accentColor = MicdGold,
                                modifier = Modifier.weight(1f),
                                subtitle = "Half-Yearly Exam"
                            )
                        }
                    }

                    // AI Teacher Launcher Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAi() }
                                .testTag("student_launch_ai_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MicdPrimary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = MicdPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = AppStrings.aiTeacher(currentLang),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (currentLang == AppLanguage.HINDI)
                                            "गणित, विज्ञान, व्याकरण के प्रश्न पूछें (हिन्दी/English)"
                                        else
                                            "Ask step-by-step Math, Science, and Grammar questions",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Study Notes Library Shortcut Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToNotes() }
                                .testTag("student_launch_notes_btn"),
                            shape = RoundedCornerShape(14.dp),
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
                                        .size(40.dp)
                                        .background(MicdSecondary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = MicdSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (currentLang == AppLanguage.HINDI) "अध्ययन नोट्स लाइब्रेरी" else "Study Notes Library",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MicdTextPrimary
                                    )
                                    Text(
                                        text = "Chapter-wise notes, PDF summaries & revision sheets",
                                        fontSize = 11.sp,
                                        color = MicdTextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MicdTextMuted
                                )
                            }
                        }
                    }

                    // Recorded Video Lectures Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToVideos() }
                                .testTag("student_launch_videos_btn"),
                            shape = RoundedCornerShape(14.dp),
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
                                        .size(40.dp)
                                        .background(Color(0xFFE91E63).copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        tint = Color(0xFFC2185B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (currentLang == AppLanguage.HINDI) "रिकॉर्डेड वीडियो लेक्चर्स" else "Recorded Video Lectures",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MicdTextPrimary
                                    )
                                    Text(
                                        text = if (currentLang == AppLanguage.HINDI)
                                            "कक्षा 1-12 के सभी विषयों के वीडियो व्याख्यान देखें"
                                        else
                                            "Watch smart classes & video recordings by teachers",
                                        fontSize = 11.sp,
                                        color = MicdTextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MicdTextMuted
                                )
                            }
                        }
                    }

                    // Student-to-Student Chat & Applications to Sir Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToChat() }
                                .testTag("student_launch_chat_btn"),
                            shape = RoundedCornerShape(14.dp),
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
                                        .size(40.dp)
                                        .background(Color(0xFF00897B).copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forum,
                                        contentDescription = null,
                                        tint = Color(0xFF00796B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (currentLang == AppLanguage.HINDI) "सहपाठी चैट व सर को आवेदन" else "Classmate Chat & Apply to Sir",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MicdTextPrimary
                                    )
                                    Text(
                                        text = if (currentLang == AppLanguage.HINDI)
                                            "सहपाठियों से चैट करें व छुट्टी/प्रमाण पत्र हेतु सर को आवेदन भेजें"
                                        else
                                            "Chat with classmates and submit leave/permission to Sir",
                                        fontSize = 11.sp,
                                        color = MicdTextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MicdTextMuted
                                )
                            }
                        }
                    }

                    // School Notices Section
                    item {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "स्कूल सूचनाएं" else "School Announcements",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MicdTextPrimary
                        )
                    }

                    items(notices.take(3), key = { it.id }) { notice ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = notice.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MicdTextPrimary
                                    )
                                    Text(notice.date, fontSize = 10.sp, color = MicdTextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(notice.content, fontSize = 11.sp, color = MicdTextSecondary, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Examination Result / Marksheet Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (examResult != null) {
                        val res = examResult!!
                        item {
                            // Official Report Card Header
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = res.exam.examName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MicdPrimary
                                            )
                                            Text(
                                                text = "Session: ${res.exam.academicYear}",
                                                fontSize = 12.sp,
                                                color = MicdTextSecondary
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (res.isPassed) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                        ) {
                                            Text(
                                                text = if (res.isPassed) "PASSED (${res.grade})" else "FAIL",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MicdCardBorder)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Marks", fontSize = 11.sp, color = MicdTextSecondary)
                                            Text("${res.totalObtained}/${res.maxMarks}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MicdTextPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Percentage", fontSize = 11.sp, color = MicdTextSecondary)
                                            Text(String.format("%.1f%%", res.percentage), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MicdPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Class Rank", fontSize = 11.sp, color = MicdTextSecondary)
                                            Text("#${res.classRank}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MicdGold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("School Rank", fontSize = 11.sp, color = MicdTextSecondary)
                                            Text("#${res.schoolRank}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MicdSecondary)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "विषय-वार अंक विवरण" else "Subject-wise Mark Distribution",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MicdTextPrimary
                            )
                        }

                        items(res.subjectMarks, key = { it.id }) { mark ->
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mark.subject, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MicdTextPrimary)
                                        Text(mark.teacherRemarks, fontSize = 11.sp, color = MicdTextSecondary)
                                    }
                                    Text(
                                        text = "${mark.marksObtained} / ${mark.maxMarks}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (mark.marksObtained >= 75) Color(0xFF2E7D32) else MicdTextPrimary
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MicdSecondary, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Examination results are being finalized by faculty.", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Homework Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Assigned Homework (${studentHomework.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MicdTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(studentHomework, key = { it.id }) { hw ->
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
                                        Text(
                                            text = hw.subject,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MicdPrimary
                                        )
                                        Text(
                                            text = "Due: ${hw.dueDate}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFD32F2F),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(hw.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MicdTextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(hw.description, fontSize = 12.sp, color = MicdTextSecondary)
                                    if (hw.attachmentContent.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("📎 Attachment: ${hw.attachmentContent}", fontSize = 11.sp, color = MicdSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Attendance History Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Present",
                            value = "${attendanceSummary.presentDays}",
                            icon = Icons.Default.CheckCircle,
                            accentColor = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Absent",
                            value = "${attendanceSummary.absentDays}",
                            icon = Icons.Default.Cancel,
                            accentColor = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Leave",
                            value = "${attendanceSummary.leaveDays}",
                            icon = Icons.Default.Schedule,
                            accentColor = Color(0xFFED6C02),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Daily Attendance Log",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MicdTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(studentAttendance, key = { it.id }) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(log.date, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MicdTextPrimary)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (log.status) {
                                            "Present" -> Color(0xFF2E7D32).copy(alpha = 0.12f)
                                            "Absent" -> Color(0xFFD32F2F).copy(alpha = 0.12f)
                                            else -> Color(0xFFED6C02).copy(alpha = 0.12f)
                                        }
                                    ) {
                                        Text(
                                            text = log.status.uppercase(),
                                            color = when (log.status) {
                                                "Present" -> Color(0xFF2E7D32)
                                                "Absent" -> Color(0xFFD32F2F)
                                                else -> Color(0xFFED6C02)
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
}
