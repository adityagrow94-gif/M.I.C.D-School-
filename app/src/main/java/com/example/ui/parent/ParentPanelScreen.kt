package com.example.ui.parent

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
import kotlinx.coroutines.flow.flowOf
import com.example.data.model.*
import com.example.data.repository.StudentExamResult
import com.example.ui.components.StatCard
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.state.SchoolViewModel
import com.example.ui.theme.*

@Composable
fun ParentPanelScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val notices by viewModel.allNotices.collectAsStateWithLifecycle()
    val homeworkList by viewModel.allHomework.collectAsStateWithLifecycle()
    val exams by viewModel.allExams.collectAsStateWithLifecycle()

    val student = selectedStudent ?: students.firstOrNull()

    val studentAttendance by (if (student != null) viewModel.getAttendanceForStudent(student.id) else kotlinx.coroutines.flow.flowOf(emptyList<AttendanceEntity>()))
        .collectAsStateWithLifecycle(emptyList())

    val attendanceSummary = remember(studentAttendance) {
        viewModel.getAttendanceSummary(studentAttendance)
    }

    val studentHomework = remember(homeworkList, student) {
        if (student == null) emptyList()
        else homeworkList.filter { it.classNumber == student.classNumber && (it.section == student.section || it.section == "All") }
    }

    var examResult by remember { mutableStateOf<StudentExamResult?>(null) }
    var showReportCardDialog by remember { mutableStateOf(false) }

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
        // Parent Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFFFE0B2), CircleShape)
                            .border(2.dp, MicdTertiary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = MicdTertiary, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "अभिभावक पोर्टल (Parent Portal)" else "Parent Portal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MicdTextPrimary
                        )
                        Text(
                            text = "Ward: ${student?.name ?: "Student"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MicdPrimary
                        )
                        Text(
                            text = "Class ${student?.classNumber}-${student?.section} • Roll #${student?.rollNumber}",
                            fontSize = 11.sp,
                            color = MicdTextSecondary
                        )
                    }

                    // Download Report Card Button
                    Button(
                        onClick = { showReportCardDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("download_report_card_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLang == AppLanguage.HINDI) "प्रगति पत्र" else "Report Card", fontSize = 11.sp)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Stat Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = if (currentLang == AppLanguage.HINDI) "उपस्थिति प्रतिशत" else "Attendance",
                        value = String.format("%.1f%%", attendanceSummary.percentage),
                        icon = Icons.Default.EventAvailable,
                        accentColor = if (attendanceSummary.percentage >= 75.0) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f),
                        subtitle = "${attendanceSummary.presentDays} Days Present"
                    )
                    StatCard(
                        title = if (currentLang == AppLanguage.HINDI) "परीक्षा प्रतिशत" else "Overall Score",
                        value = if (examResult != null) String.format("%.1f%%", examResult!!.percentage) else "85.0%",
                        icon = Icons.Default.Grade,
                        accentColor = MicdGold,
                        modifier = Modifier.weight(1f),
                        subtitle = if (examResult != null) "Grade: ${examResult!!.grade}" else "Grade: A"
                    )
                }
            }

            // Academic Progress & Remarks Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = MicdSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "शिक्षक टिप्पणी एवं शैक्षणिक समीक्षा" else "Teacher Remarks & Academic Insights",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MicdTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = examResult?.remarks ?: "Consistent academic discipline. Actively participates in classroom discussions.",
                            fontSize = 12.sp,
                            color = MicdTextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Class Rank: #${examResult?.classRank ?: 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MicdPrimary)
                            Text("School Rank: #${examResult?.schoolRank ?: 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MicdSecondary)
                            Text(
                                text = if (examResult?.isPassed == true) "Status: PASSED" else "Status: In Progress",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            // Ward Profile Details Card
            student?.let { stu ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "छात्र आधिकारिक विवरण" else "Official Student Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MicdTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Father: ${stu.fatherName} • Mother: ${stu.motherName}", fontSize = 12.sp, color = MicdTextSecondary)
                            Text("Date of Birth: ${stu.dob} • Contact: ${stu.contactNumber}", fontSize = 12.sp, color = MicdTextSecondary)
                            Text("Address: ${stu.address}", fontSize = 11.sp, color = MicdTextMuted)
                        }
                    }
                }
            }

            // Ward Homework Status
            item {
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "बच्चे का गृहकार्य कार्यभार" else "Assigned Homework to Ward",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MicdTextPrimary
                )
            }

            items(studentHomework, key = { it.id }) { hw ->
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
                            Text(hw.subject, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MicdPrimary)
                            Text("Due: ${hw.dueDate}", fontSize = 11.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
                        }
                        Text(hw.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MicdTextPrimary)
                        Text(hw.description, fontSize = 11.sp, color = MicdTextSecondary)
                    }
                }
            }

            // Parent Notices
            item {
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "अभिभावक विशेष सूचनाएं एवं PTM" else "PTM & Parent Announcements",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MicdTextPrimary
                )
            }

            val ptmNotices = notices.filter { it.category == "Parent Meeting" || it.category == "Result" || it.isUrgent }
            items(ptmNotices, key = { it.id }) { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                            Text(notice.date, fontSize = 10.sp, color = MicdTextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notice.content, fontSize = 11.sp, color = MicdTextPrimary, lineHeight = 16.sp)
                    }
                }
            }
        }
    }

    // Official Report Card Modal Dialog
    if (showReportCardDialog && student != null) {
        OfficialReportCardDialog(
            student = student,
            result = examResult,
            currentLang = currentLang,
            onDismiss = { showReportCardDialog = false }
        )
    }
}

@Composable
fun OfficialReportCardDialog(
    student: StudentEntity,
    result: StudentExamResult?,
    currentLang: AppLanguage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("M.I.C.D SCHOOL", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MicdPrimary)
                Text("OFFICIAL PROGRESS REPORT CARD", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MicdSecondary)
                Text("Affiliated to Educational Board • Session 2024-2025", fontSize = 10.sp, color = MicdTextMuted)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Divider(color = MicdCardBorder)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Student Name: ${student.name}", fontWeight = FontWeight.Bold)
                    Text("Class: ${student.classNumber}-${student.section} • Roll No: ${student.rollNumber}")
                    Text("Admission No: ${student.admissionNo} • Student ID: ${student.studentId}")
                    Text("Father's Name: ${student.fatherName}")
                    Divider(color = MicdCardBorder, modifier = Modifier.padding(vertical = 4.dp))
                }

                if (result != null) {
                    item {
                        Text("Subject Breakdown:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    items(result.subjectMarks) { m ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(m.subject, fontSize = 12.sp)
                            Text("${m.marksObtained} / ${m.maxMarks}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    item {
                        Divider(color = MicdCardBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Marks:", fontWeight = FontWeight.Bold)
                            Text("${result.totalObtained} / ${result.maxMarks}", fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Overall Percentage:", fontWeight = FontWeight.Bold)
                            Text(String.format("%.1f%%", result.percentage), fontWeight = FontWeight.Bold, color = MicdPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Final Grade:", fontWeight = FontWeight.Bold)
                            Text(result.grade, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Class Rank: #${result.classRank}", fontWeight = FontWeight.SemiBold)
                            Text("School Rank: #${result.schoolRank}", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Teacher Remarks: \"${result.remarks}\"", fontSize = 11.sp, color = MicdTextSecondary)
                    }
                } else {
                    item {
                        Text("Exam marks calculation in progress.")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)) {
                Text("Close")
            }
        }
    )
}
