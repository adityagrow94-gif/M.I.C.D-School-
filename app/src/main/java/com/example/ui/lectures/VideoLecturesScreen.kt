package com.example.ui.lectures

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VideoLectureEntity
import com.example.ui.state.AppLanguage
import com.example.ui.state.SchoolViewModel
import com.example.ui.state.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLecturesScreen(
    viewModel: SchoolViewModel,
    onBack: () -> Unit,
    onOpenAiTeacher: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val allLectures by viewModel.allVideoLectures.collectAsStateWithLifecycle()
    val activePlayingLecture by viewModel.selectedVideo.collectAsStateWithLifecycle()

    var selectedClass by remember { mutableStateOf(selectedStudent?.classNumber ?: 10) }
    var selectedSubject by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddLectureDialog by remember { mutableStateOf(false) }

    // Filtered Lectures
    val filteredLectures = remember(allLectures, selectedClass, selectedSubject, searchQuery) {
        allLectures.filter { lecture ->
            val matchClass = lecture.classNumber == selectedClass
            val matchSubject = selectedSubject == "All" || lecture.subject.equals(selectedSubject, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() ||
                    lecture.title.contains(searchQuery, ignoreCase = true) ||
                    lecture.chapterName.contains(searchQuery, ignoreCase = true) ||
                    lecture.teacherName.contains(searchQuery, ignoreCase = true)
            matchClass && matchSubject && matchQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "रिकॉर्डेड वीडियो लेक्चर्स" else "Recorded Video Lectures",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (currentLang == AppLanguage.HINDI)
                                "कक्षा 1 से 12 • विषयवार डिजिटल कक्षाएं"
                            else
                                "Classes 1 to 12 • Digital Smart Classrooms",
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
                actions = {
                    if (currentRole == UserRole.TEACHER || currentRole == UserRole.ADMIN) {
                        FilledTonalButton(
                            onClick = { showAddLectureDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MicdGold,
                                contentColor = Color(0xFF1A2332)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("add_video_lecture_btn")
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLang == AppLanguage.HINDI) "+ वीडियो जोड़ें" else "+ Add Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
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
            // Active Video Player Card if any lecture is selected
            activePlayingLecture?.let { lecture ->
                InteractiveVideoPlayerCard(
                    lecture = lecture,
                    currentLang = currentLang,
                    onClose = { viewModel.selectVideo(null) },
                    onAskAiTeacher = onOpenAiTeacher
                )
            }

            // Filters & Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Class selector pill row (Classes 1 to 12)
                item {
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "कक्षा चुनें (Class 1-12)" else "Select Class (1 to 12)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MicdTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items((1..12).toList()) { classNum ->
                                FilterChip(
                                    selected = selectedClass == classNum,
                                    onClick = { selectedClass = classNum },
                                    label = { Text("Class $classNum", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MicdPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Subject filters & Search row
                item {
                    Column {
                        val subjects = listOf("All", "Mathematics", "Science", "Physics", "Chemistry", "English", "Hindi")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(subjects) { subj ->
                                FilterChip(
                                    selected = selectedSubject == subj,
                                    onClick = { selectedSubject = subj },
                                    label = { Text(subj, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    if (currentLang == AppLanguage.HINDI) "पाठ या शीर्षक से खोजें..." else "Search by chapter or title...",
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MicdTextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                // Header with Count
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI)
                                "कक्षा $selectedClass उपलब्ध लेक्चर्स (${filteredLectures.size})"
                            else
                                "Class $selectedClass Lectures (${filteredLectures.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MicdTextPrimary
                        )

                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "HD रिकॉर्डिंग" else "HD Recorded",
                            fontSize = 11.sp,
                            color = MicdPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Video Lecture Cards
                if (filteredLectures.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.OndemandVideo, contentDescription = null, tint = MicdTextSecondary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI)
                                        "कक्षा $selectedClass के लिए कोई लेक्चर उपलब्ध नहीं है"
                                    else
                                        "No lectures found for Class $selectedClass in $selectedSubject",
                                    color = MicdTextSecondary,
                                    fontSize = 13.sp
                                )
                                if (currentRole == UserRole.TEACHER || currentRole == UserRole.ADMIN) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { showAddLectureDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
                                    ) {
                                        Text("+ पहला वीडियो लेक्चर अपलोड करें")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filteredLectures, key = { it.id }) { lecture ->
                        VideoLectureCard(
                            lecture = lecture,
                            currentLang = currentLang,
                            isCurrentlyPlaying = activePlayingLecture?.id == lecture.id,
                            onPlay = { viewModel.selectVideo(lecture) },
                            onDelete = { viewModel.deleteVideoLecture(lecture.id) },
                            canDelete = currentRole == UserRole.ADMIN
                        )
                    }
                }
            }
        }
    }

    // Add Video Lecture Dialog
    if (showAddLectureDialog) {
        AddVideoLectureDialog(
            defaultClass = selectedClass,
            currentLang = currentLang,
            onDismiss = { showAddLectureDialog = false },
            onAdd = { title, subject, classNum, chNum, chName, teacher, duration, url, desc ->
                viewModel.addVideoLecture(title, subject, classNum, chNum, chName, teacher, duration, url, desc)
                showAddLectureDialog = false
            }
        )
    }
}

@Composable
fun VideoLectureCard(
    lecture: VideoLectureEntity,
    currentLang: AppLanguage,
    isCurrentlyPlaying: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("video_card_${lecture.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentlyPlaying) 4.dp else 1.5.dp),
        border = if (isCurrentlyPlaying) androidx.compose.foundation.BorderStroke(2.dp, MicdPrimary) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail / Header preview banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = when (lecture.subject.lowercase()) {
                                "mathematics" -> listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                                "physics" -> listOf(Color(0xFF311B92), Color(0xFF512DA8))
                                "chemistry", "science" -> listOf(Color(0xFF004D40), Color(0xFF00796B))
                                "hindi" -> listOf(Color(0xFFB71C1C), Color(0xFFD32F2F))
                                else -> listOf(Color(0xFF263238), Color(0xFF455A64))
                            }
                        )
                    )
            ) {
                // Play Icon Overlay
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCurrentlyPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MicdGold,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Duration badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${lecture.durationMinutes} min", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Subject Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MicdGold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = lecture.subject,
                        color = Color(0xFF1A2332),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Body Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = lecture.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MicdTextPrimary,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ch ${lecture.chapterNumber}: ${lecture.chapterName}",
                    fontSize = 12.sp,
                    color = MicdPrimary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = lecture.description,
                    fontSize = 11.sp,
                    color = MicdTextSecondary,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MicdTextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(lecture.teacherName, fontSize = 11.sp, color = MicdTextSecondary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = MicdTextMuted, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${lecture.viewCount} views", fontSize = 11.sp, color = MicdTextMuted)

                        if (canDelete) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveVideoPlayerCard(
    lecture: VideoLectureEntity,
    currentLang: AppLanguage,
    onClose: () -> Unit,
    onAskAiTeacher: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionSeconds by remember { mutableFloatStateOf(420f) } // 7 mins in
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    val totalSeconds = (lecture.durationMinutes * 60).toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101827)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Player Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(0xFFE53935)) {
                        Box(modifier = Modifier.size(8.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "M.I.C.D SMART PLAYER",
                        color = MicdGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Screen simulation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = MicdGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lecture.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "Instructor: ${lecture.teacherName} • Class ${lecture.classNumber}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrubbing Slider
            Slider(
                value = currentPositionSeconds,
                onValueChange = { currentPositionSeconds = it },
                valueRange = 0f..totalSeconds,
                colors = SliderDefaults.colors(
                    thumbColor = MicdGold,
                    activeTrackColor = MicdGold,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Time & Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentMin = (currentPositionSeconds / 60).toInt()
                val currentSec = (currentPositionSeconds % 60).toInt()
                val totalMin = (totalSeconds / 60).toInt()
                val totalSec = (totalSeconds % 60).toInt()

                Text(
                    text = String.format("%02d:%02d / %02d:%02d", currentMin, currentSec, totalMin, totalSec),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // -10s
                    IconButton(
                        onClick = { currentPositionSeconds = (currentPositionSeconds - 10f).coerceAtLeast(0f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White)
                    }

                    // Play/Pause
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(38.dp)
                            .background(MicdGold, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color(0xFF101827)
                        )
                    }

                    // +10s
                    IconButton(
                        onClick = { currentPositionSeconds = (currentPositionSeconds + 10f).coerceAtMost(totalSeconds) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White)
                    }

                    // Speed Toggle
                    FilledTonalButton(
                        onClick = {
                            playbackSpeed = when (playbackSpeed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 2.0f
                                else -> 1.0f
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("${playbackSpeed}x", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Pills: Ask AI Teacher about lecture & Download notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAskAiTeacher,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MicdGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MicdGold),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentLang == AppLanguage.HINDI) "AI शिक्षक से पूछें" else "Ask AI Teacher", fontSize = 11.sp)
                }

                Button(
                    onClick = { /* simulated notes download */ },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentLang == AppLanguage.HINDI) "लेक्चर नोट्स (PDF)" else "Lecture Notes", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddVideoLectureDialog(
    defaultClass: Int,
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onAdd: (title: String, subject: String, classNum: Int, chNum: Int, chName: String, teacher: String, duration: Int, url: String, desc: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var classNum by remember { mutableIntStateOf(defaultClass) }
    var chapterNumber by remember { mutableStateOf("1") }
    var chapterName by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("45") }
    var videoUrl by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentLang == AppLanguage.HINDI) "नया वीडियो लेक्चर जोड़ें" else "Add New Video Lecture",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Lecture Title (शीर्षक)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (विषय)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = classNum.toString(),
                        onValueChange = { classNum = it.toIntOrNull() ?: classNum },
                        label = { Text("Class (1-12)") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chapterNumber,
                        onValueChange = { chapterNumber = it },
                        label = { Text("Ch #") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = chapterName,
                        onValueChange = { chapterName = it },
                        label = { Text("Chapter Name") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("Teacher Name (शिक्षक)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("Video Stream / MP4 URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (विवरण)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && chapterName.isNotBlank()) {
                        onAdd(
                            title.trim(),
                            subject.trim(),
                            classNum,
                            chapterNumber.toIntOrNull() ?: 1,
                            chapterName.trim(),
                            teacherName.trim(),
                            durationMinutes.toIntOrNull() ?: 45,
                            videoUrl.trim(),
                            description.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
            ) {
                Text(if (currentLang == AppLanguage.HINDI) "अपलोड करें (Upload)" else "Upload Lecture")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
