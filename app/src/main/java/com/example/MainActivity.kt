package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.ai.AiTeacherScreen
import com.example.ui.components.RoleSwitcherDialog
import com.example.ui.components.SchoolTopBar
import com.example.ui.notes.NotesLibraryScreen
import com.example.ui.parent.ParentPanelScreen
import com.example.ui.ranking.TopperRankingScreen
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.state.SchoolViewModel
import com.example.ui.state.UserRole
import com.example.ui.student.StudentPanelScreen
import com.example.ui.teacher.TeacherPanelScreen
import com.example.ui.theme.MicdBackground
import com.example.ui.theme.MicdPrimary
import com.example.ui.theme.MicdSchoolTheme
import com.example.ui.notifications.AdminNotificationPanelScreen
import com.example.ui.notifications.NotificationSettingsScreen
import com.example.ui.auth.AnimatedLoginScreen
import com.example.ui.chat.SchoolCommunityChatScreen
import com.example.ui.lectures.VideoLecturesScreen

enum class AppDestination {
    PANEL,
    TOPPERS,
    NOTES,
    AI_TEACHER,
    NOTIFICATIONS,
    ADMIN_NOTIFICATIONS,
    CHAT,
    VIDEO_LECTURES,
    LOGIN
}

class MainActivity : ComponentActivity() {
    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val targetUrl = intent?.getStringExtra("targetUrl") ?: intent?.data?.toString()

        setContent {
            MicdSchoolTheme {
                MainSchoolApp(
                    viewModel = viewModel,
                    initialTargetUrl = targetUrl
                )
            }
        }
    }
}

@Composable
fun MainSchoolApp(
    viewModel: SchoolViewModel,
    initialTargetUrl: String? = null
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val selectedTeacher by viewModel.selectedTeacher.collectAsStateWithLifecycle()

    val initialDestination = remember(initialTargetUrl) {
        when {
            initialTargetUrl?.contains("notification") == true -> {
                if (currentRole == UserRole.ADMIN) AppDestination.ADMIN_NOTIFICATIONS else AppDestination.NOTIFICATIONS
            }
            initialTargetUrl?.contains("result") == true || initialTargetUrl?.contains("topper") == true -> AppDestination.TOPPERS
            initialTargetUrl?.contains("homework") == true || initialTargetUrl?.contains("note") == true -> AppDestination.NOTES
            initialTargetUrl?.contains("ai") == true -> AppDestination.AI_TEACHER
            else -> AppDestination.PANEL
        }
    }
    var currentDestination by remember { mutableStateOf(initialDestination) }
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }

    // Custom back handler to return to Panel if in sub-screen
    if (currentDestination != AppDestination.PANEL) {
        BackHandler { currentDestination = AppDestination.PANEL }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentDestination == AppDestination.PANEL) {
                SchoolTopBar(
                    currentRole = currentRole,
                    currentLang = currentLang,
                    onToggleLang = { viewModel.toggleLanguage() },
                    onOpenRoleSwitcher = { showRoleSwitcherDialog = true },
                    onOpenAiChat = { currentDestination = AppDestination.AI_TEACHER },
                    onOpenNotifications = {
                        if (currentRole == UserRole.ADMIN) {
                            currentDestination = AppDestination.ADMIN_NOTIFICATIONS
                        } else {
                            currentDestination = AppDestination.NOTIFICATIONS
                        }
                    },
                    onOpenLogin = { currentDestination = AppDestination.LOGIN }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                // 1. Role-based Panel tab
                val panelTitle = when (currentRole) {
                    UserRole.ADMIN -> AppStrings.adminPanel(currentLang)
                    UserRole.TEACHER -> AppStrings.teacherPanel(currentLang)
                    UserRole.STUDENT -> AppStrings.studentPanel(currentLang)
                    UserRole.PARENT -> AppStrings.parentPanel(currentLang)
                }
                val panelIcon = when (currentRole) {
                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                    UserRole.TEACHER -> Icons.Default.CastForEducation
                    UserRole.STUDENT -> Icons.Default.Person
                    UserRole.PARENT -> Icons.Default.FamilyRestroom
                }

                NavigationBarItem(
                    selected = currentDestination == AppDestination.PANEL,
                    onClick = { currentDestination = AppDestination.PANEL },
                    icon = { Icon(panelIcon, contentDescription = panelTitle) },
                    label = { Text(panelTitle.split(" ").firstOrNull() ?: panelTitle, fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_panel_tab")
                )

                // 2. Video Lectures tab
                NavigationBarItem(
                    selected = currentDestination == AppDestination.VIDEO_LECTURES,
                    onClick = { currentDestination = AppDestination.VIDEO_LECTURES },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Video Lectures") },
                    label = { Text(if (currentLang == AppLanguage.HINDI) "वीडियो" else "Lectures", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_videos_tab")
                )

                // 3. Student Chat & Sir Application tab
                NavigationBarItem(
                    selected = currentDestination == AppDestination.CHAT,
                    onClick = { currentDestination = AppDestination.CHAT },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Chat & Apply") },
                    label = { Text(if (currentLang == AppLanguage.HINDI) "चैट/आवेदन" else "Chat/Apply", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_chat_tab")
                )

                // 4. Notes Library tab
                NavigationBarItem(
                    selected = currentDestination == AppDestination.NOTES,
                    onClick = { currentDestination = AppDestination.NOTES },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Notes") },
                    label = { Text(if (currentLang == AppLanguage.HINDI) "नोट्स" else "Notes", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_notes_tab")
                )

                // 5. M.I.C.D AI Teacher tab
                NavigationBarItem(
                    selected = currentDestination == AppDestination.AI_TEACHER,
                    onClick = { currentDestination = AppDestination.AI_TEACHER },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Teacher") },
                    label = { Text(if (currentLang == AppLanguage.HINDI) "एआई शिक्षक" else "AI Teacher", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_ai_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MicdBackground)
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.PANEL -> {
                    when (currentRole) {
                        UserRole.ADMIN -> {
                            AdminPanelScreen(viewModel = viewModel)
                        }
                        UserRole.TEACHER -> {
                            TeacherPanelScreen(viewModel = viewModel)
                        }
                        UserRole.STUDENT -> {
                            StudentPanelScreen(
                                viewModel = viewModel,
                                onNavigateToAi = { currentDestination = AppDestination.AI_TEACHER },
                                onNavigateToNotes = { currentDestination = AppDestination.NOTES },
                                onNavigateToChat = { currentDestination = AppDestination.CHAT },
                                onNavigateToVideos = { currentDestination = AppDestination.VIDEO_LECTURES }
                            )
                        }
                        UserRole.PARENT -> {
                            ParentPanelScreen(viewModel = viewModel)
                        }
                    }
                }

                AppDestination.TOPPERS -> {
                    TopperRankingScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.PANEL }
                    )
                }

                AppDestination.NOTES -> {
                    NotesLibraryScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.PANEL }
                    )
                }

                AppDestination.AI_TEACHER -> {
                    AiTeacherScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.PANEL }
                    )
                }

                AppDestination.NOTIFICATIONS -> {
                    NotificationSettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.PANEL }
                    )
                }

                AppDestination.ADMIN_NOTIFICATIONS -> {
                    AdminNotificationPanelScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.PANEL },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AppDestination.CHAT -> {
                    SchoolCommunityChatScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.PANEL }
                    )
                }

                AppDestination.VIDEO_LECTURES -> {
                    VideoLecturesScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.PANEL },
                        onOpenAiTeacher = { currentDestination = AppDestination.AI_TEACHER }
                    )
                }

                AppDestination.LOGIN -> {
                    AnimatedLoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { currentDestination = AppDestination.PANEL }
                    )
                }
            }
        }
    }

    // Role Switcher Dialog
    if (showRoleSwitcherDialog) {
        RoleSwitcherDialog(
            currentRole = currentRole,
            onRoleSelected = { newRole ->
                viewModel.switchRole(newRole)
                currentDestination = AppDestination.PANEL
            },
            students = students,
            selectedStudent = selectedStudent,
            onStudentSelected = { viewModel.selectStudent(it) },
            teachers = teachers,
            selectedTeacher = selectedTeacher,
            onTeacherSelected = { viewModel.selectTeacher(it) },
            currentLang = currentLang,
            onDismiss = { showRoleSwitcherDialog = false }
        )
    }
}
