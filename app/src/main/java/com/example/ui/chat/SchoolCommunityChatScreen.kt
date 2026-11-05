package com.example.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.PeerChatMessageEntity
import com.example.data.model.StudentApplicationEntity
import com.example.data.model.StudentEntity
import com.example.ui.state.AppLanguage
import com.example.ui.state.SchoolViewModel
import com.example.ui.state.UserRole
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolCommunityChatScreen(
    viewModel: SchoolViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val allTeachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val allChatMessages by viewModel.allChatMessages.collectAsStateWithLifecycle()
    val allApplications by viewModel.allApplications.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Student Chat, 1: Application to Sir, 2: Class Group
    var activeChatStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var showNewApplicationDialog by remember { mutableStateOf(false) }

    // Filter classmates for the current student's class
    val studentClass = selectedStudent?.classNumber ?: 10
    val studentSection = selectedStudent?.section ?: "A"
    val classmates = remember(allStudents, studentClass, studentSection, selectedStudent) {
        allStudents.filter {
            it.classNumber == studentClass &&
            it.section == studentSection &&
            it.id != selectedStudent?.id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "स्कूल संचार एवं आवेदन केंद्र" else "School Chat & Applications",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (currentLang == AppLanguage.HINDI)
                                "सहपाठी चैट • सर को आवेदन • कक्षा संवाद"
                            else
                                "Classmate Chat • Apply to Sir • Class Board",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MicdPrimary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MicdBackground)
        ) {
            // Sub Tabs: 1. Student Chat, 2. Applications to Sir, 3. Class Group
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = MicdPrimary,
                divider = {}
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = {
                        activeTab = 0
                        activeChatStudent = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "सहपाठी चैट" else "Student Chat",
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "सर को आवेदन" else "Apply to Sir",
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "कक्षा ग्रुप" else "Class Group",
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeTab) {
                0 -> {
                    // Student-to-Student Direct Chat
                    if (activeChatStudent == null) {
                        ClassmatesListView(
                            classmates = classmates,
                            studentClass = studentClass,
                            studentSection = studentSection,
                            currentLang = currentLang,
                            onSelectStudent = { activeChatStudent = it }
                        )
                    } else {
                        val recipient = activeChatStudent!!
                        DirectChatThreadView(
                            recipientName = recipient.name,
                            recipientSubtitle = "Roll No: ${recipient.rollNumber} • Class ${recipient.classNumber}-${recipient.section}",
                            recipientId = recipient.studentId,
                            viewModel = viewModel,
                            chatMessages = allChatMessages.filter {
                                (it.senderId == selectedStudent?.studentId && it.recipientId == recipient.studentId) ||
                                (it.senderId == recipient.studentId && it.recipientId == selectedStudent?.studentId)
                            },
                            currentLang = currentLang,
                            onBackToStudentList = { activeChatStudent = null }
                        )
                    }
                }

                1 -> {
                    // Student Applications to Sir (Leave, Fee Concession, Bonafide, Permission)
                    ApplicationsToSirView(
                        viewModel = viewModel,
                        applications = allApplications,
                        currentRole = currentRole,
                        currentLang = currentLang,
                        selectedStudent = selectedStudent,
                        onOpenNewApplication = { showNewApplicationDialog = true }
                    )
                }

                2 -> {
                    // Whole Class Group Chat Board
                    DirectChatThreadView(
                        recipientName = "Class $studentClass-$studentSection Official Group",
                        recipientSubtitle = "All students and Class Teacher of $studentClass-$studentSection",
                        recipientId = "ALL_CLASS_${studentClass}${studentSection}",
                        viewModel = viewModel,
                        chatMessages = allChatMessages.filter {
                            it.chatType == "CLASS_GROUP" && it.classNumber == studentClass && it.section == studentSection
                        },
                        currentLang = currentLang,
                        chatType = "CLASS_GROUP",
                        onBackToStudentList = {}
                    )
                }
            }
        }
    }

    // New Application to Sir Dialog
    if (showNewApplicationDialog) {
        CreateApplicationDialog(
            currentLang = currentLang,
            onDismiss = { showNewApplicationDialog = false },
            onSubmit = { type, subject, reason, start, end ->
                viewModel.submitStudentApplication(type, subject, reason, start, end)
                showNewApplicationDialog = false
            }
        )
    }
}

@Composable
fun ClassmatesListView(
    classmates: List<StudentEntity>,
    studentClass: Int,
    studentSection: String,
    currentLang: AppLanguage,
    onSelectStudent: (StudentEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MicdPrimary.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = MicdPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (currentLang == AppLanguage.HINDI)
                            "कक्षा $studentClass-$studentSection सहपाठी चैट रूम"
                        else
                            "Class $studentClass-$studentSection Peer Study Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MicdPrimary
                    )
                    Text(
                        text = if (currentLang == AppLanguage.HINDI)
                            "पढ़ाई, होमवर्क और नोट्स के लिए किसी भी सहपाठी को संदेश भेजें"
                        else
                            "Select any classmate to chat regarding studies, notes, or homework",
                        fontSize = 11.sp,
                        color = MicdTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (currentLang == AppLanguage.HINDI) "आपके सहपाठी (${classmates.size})" else "Your Classmates (${classmates.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MicdTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (classmates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "कोई सहपाठी नहीं मिला" else "No classmates found in this section",
                    color = MicdTextSecondary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(classmates, key = { it.id }) { student ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStudent(student) },
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
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (student.gender == "Girl") Color(0xFFFCE4EC) else Color(0xFFE3F2FD)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.name.firstOrNull()?.toString() ?: "S",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (student.gender == "Girl") Color(0xFFC2185B) else MicdPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MicdTextPrimary
                                )
                                Text(
                                    text = "Roll No: ${student.rollNumber} • ${student.gender} • ID: ${student.studentId}",
                                    fontSize = 11.sp,
                                    color = MicdTextSecondary
                                )
                            }

                            FilledTonalButton(
                                onClick = { onSelectStudent(student) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MicdPrimary.copy(alpha = 0.12f),
                                    contentColor = MicdPrimary
                                )
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (currentLang == AppLanguage.HINDI) "चैट करें" else "Chat", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DirectChatThreadView(
    recipientName: String,
    recipientSubtitle: String,
    recipientId: String,
    viewModel: SchoolViewModel,
    chatMessages: List<PeerChatMessageEntity>,
    currentLang: AppLanguage,
    chatType: String = "STUDENT_TO_STUDENT",
    onBackToStudentList: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (chatType == "STUDENT_TO_STUDENT") {
                    IconButton(onClick = onBackToStudentList) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MicdPrimary)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MicdPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (chatType == "CLASS_GROUP") Icons.Default.Groups else Icons.Default.Person,
                        contentDescription = null,
                        tint = MicdPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MicdTextPrimary
                    )
                    Text(
                        text = recipientSubtitle,
                        fontSize = 11.sp,
                        color = MicdTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Online", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = if (currentLang == AppLanguage.HINDI) {
                listOf(
                    "नमस्ते! क्या आपने आज का होमवर्क पूरा किया?",
                    "कृपया विज्ञान पाठ के नोट्स साझा करें।",
                    "कल की परीक्षा की तैयारी कैसी है?",
                    "गणित के प्रश्न 4 में सहायता चाहिए।"
                )
            } else {
                listOf(
                    "Hey! Did you complete today's homework?",
                    "Can you share science chapter notes?",
                    "Ready for tomorrow's unit test?",
                    "Need help with Math Exercise 4.2."
                )
            }
            items(suggestions) { chip ->
                SuggestionChip(
                    onClick = { textInput = chip },
                    label = { Text(chip, fontSize = 11.sp) }
                )
            }
        }

        // Chat Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = false,
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI)
                                "बातचीत शुरू करने के लिए पहला संदेश भेजें 👋"
                            else
                                "Say hello to start the study conversation 👋",
                            color = MicdTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(chatMessages, key = { it.id }) { msg ->
                    val isMyMsg = msg.senderRole == "STUDENT" && (msg.recipientId == recipientId || chatType == "CLASS_GROUP")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isMyMsg) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMyMsg) 16.dp else 4.dp,
                                bottomEnd = if (isMyMsg) 4.dp else 16.dp
                            ),
                            color = if (isMyMsg) MicdPrimary else Color.White,
                            tonalElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (!isMyMsg) {
                                    Text(
                                        text = msg.senderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (msg.senderRole == "TEACHER") Color(0xFFD32F2F) else MicdGold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(
                                    text = msg.message,
                                    color = if (isMyMsg) Color.White else MicdTextPrimary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = timeFormat.format(Date(msg.timestamp)),
                                    fontSize = 9.sp,
                                    color = if (isMyMsg) Color.White.copy(alpha = 0.7f) else MicdTextMuted,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "संदेश लिखें..." else "Type message...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendPeerChatMessage(
                                recipientId = recipientId,
                                recipientName = recipientName,
                                message = textInput.trim(),
                                chatType = chatType
                            )
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MicdPrimary, CircleShape)
                        .testTag("send_chat_msg_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicationsToSirView(
    viewModel: SchoolViewModel,
    applications: List<StudentApplicationEntity>,
    currentRole: UserRole,
    currentLang: AppLanguage,
    selectedStudent: StudentEntity?,
    onOpenNewApplication: () -> Unit
) {
    var selectedApplicationToReview by remember { mutableStateOf<StudentApplicationEntity?>(null) }
    var reviewRemarks by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Hero Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        .clip(CircleShape)
                        .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = Color(0xFFE65100))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "सर को ऑनलाइन आवेदन पत्र" else "Formal Application to Sir",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MicdTextPrimary
                    )
                    Text(
                        text = if (currentLang == AppLanguage.HINDI)
                            "छुट्टी, फीस छूट, प्रमाण पत्र या सहायता हेतु सर को आवेदन भेजें"
                        else
                            "Submit leave, fee concession, or certificates directly to Teachers & Principal",
                        fontSize = 11.sp,
                        color = MicdTextSecondary
                    )
                }

                Button(
                    onClick = onOpenNewApplication,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("new_application_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentLang == AppLanguage.HINDI) "+ नया आवेदन" else "+ Apply", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (currentLang == AppLanguage.HINDI) "आवेदन पत्र सूची (${applications.size})" else "Submitted Applications (${applications.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MicdTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (applications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "अभी तक कोई आवेदन दर्ज नहीं है" else "No applications submitted yet",
                    color = MicdTextSecondary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(applications, key = { it.id }) { app ->
                    val statusColor = when (app.status) {
                        "APPROVED" -> Color(0xFF2E7D32)
                        "REJECTED" -> Color(0xFFC62828)
                        else -> Color(0xFFEF6C00)
                    }

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
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = app.status,
                                        color = statusColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = app.submittedDate,
                                    fontSize = 11.sp,
                                    color = MicdTextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = app.subject,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MicdTextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "By: ${app.studentName} • Class ${app.classNumber}-${app.section} (Roll ${app.rollNumber})",
                                fontSize = 11.sp,
                                color = MicdTextSecondary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = app.reason,
                                fontSize = 12.sp,
                                color = MicdTextPrimary.copy(alpha = 0.85f)
                            )

                            if (app.startDate.isNotBlank() && app.endDate.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MicdPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Duration: ${app.startDate} to ${app.endDate}",
                                        fontSize = 11.sp,
                                        color = MicdPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (app.reviewedBy.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MicdBackground,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Reviewed by: ${app.reviewedBy} on ${app.reviewedDate}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MicdTextPrimary
                                        )
                                        if (app.teacherRemarks.isNotBlank()) {
                                            Text(
                                                text = "Remarks: ${app.teacherRemarks}",
                                                fontSize = 11.sp,
                                                color = MicdTextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // If Teacher or Admin, show review actions
                            if (currentRole == UserRole.TEACHER || currentRole == UserRole.ADMIN) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            viewModel.reviewStudentApplication(
                                                app.id,
                                                "APPROVED",
                                                "Approved by Sir with best wishes."
                                            )
                                        }
                                    ) {
                                        Text("Approve (स्वीकार करें)", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(
                                        onClick = {
                                            viewModel.reviewStudentApplication(
                                                app.id,
                                                "REJECTED",
                                                "Please provide proper medical certificate."
                                            )
                                        }
                                    ) {
                                        Text("Reject (अस्वीकार)", color = Color(0xFFC62828))
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

@Composable
fun CreateApplicationDialog(
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (type: String, subject: String, reason: String, start: String, end: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("LEAVE") }
    var subject by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("2024-11-06") }
    var endDate by remember { mutableStateOf("2024-11-07") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentLang == AppLanguage.HINDI) "सर को नया आवेदन पत्र लिखें" else "Write Application to Sir",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "आवेदन का प्रकार चुनें" else "Select Application Type",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Type Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "LEAVE" to (if (currentLang == AppLanguage.HINDI) "छुट्टी" else "Leave"),
                        "FEE_CONCESSION" to (if (currentLang == AppLanguage.HINDI) "फीस छूट" else "Fee"),
                        "BONAFIDE" to (if (currentLang == AppLanguage.HINDI) "प्रमाण पत्र" else "Bonafide"),
                        "GENERAL" to (if (currentLang == AppLanguage.HINDI) "सामान्य" else "General")
                    ).forEach { (typeKey, label) ->
                        FilterChip(
                            selected = selectedType == typeKey,
                            onClick = {
                                selectedType = typeKey
                                if (subject.isBlank()) {
                                    subject = when (typeKey) {
                                        "LEAVE" -> if (currentLang == AppLanguage.HINDI) "बीमारी के कारण 2 दिन का अवकाश प्रार्थना पत्र" else "Application for Sick Leave"
                                        "FEE_CONCESSION" -> if (currentLang == AppLanguage.HINDI) "फीस में रियायत हेतु प्रार्थना पत्र" else "Application for Fee Concession"
                                        "BONAFIDE" -> if (currentLang == AppLanguage.HINDI) "बोनाफाइड सर्टिफिकेट जारी करने हेतु पत्र" else "Request for Bonafide Certificate"
                                        else -> if (currentLang == AppLanguage.HINDI) "आदरणीय सर को संदेश" else "Message to Respected Sir"
                                    }
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(if (currentLang == AppLanguage.HINDI) "विषय (Subject)" else "Subject", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(if (currentLang == AppLanguage.HINDI) "विस्तृत कारण / संदेश (Reason)" else "Detailed Reason", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 4
                )

                if (selectedType == "LEAVE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("From Date", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("To Date", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank() && reason.isNotBlank()) {
                        onSubmit(selectedType, subject.trim(), reason.trim(), startDate, endDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
            ) {
                Text(if (currentLang == AppLanguage.HINDI) "जमा करें (Submit)" else "Submit Application")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
