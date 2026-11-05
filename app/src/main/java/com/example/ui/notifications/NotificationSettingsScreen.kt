package com.example.ui.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.MedianNotificationManager
import com.example.ui.state.AppLanguage
import com.example.ui.state.SchoolViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: SchoolViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasNotificationPermission.collectAsStateWithLifecycle()
    val isMedianBridge by viewModel.isMedianBridgeAvailable.collectAsStateWithLifecycle()
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()

    // Android 13+ Runtime Notification Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateNotificationPermission(isGranted)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MicdPrimary,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "पुश सूचना सेटिंग्स" else "Push Notification Settings",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "M.I.C.D Native & Median.co FCM Bridge",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MicdBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Permission & Median Bridge Status Card
            item {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (hasPermission) Color(0xFF2E7D32).copy(alpha = 0.12f)
                                            else Color(0xFFD32F2F).copy(alpha = 0.12f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (hasPermission) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        tint = if (hasPermission) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Android Notification Permission",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MicdTextPrimary
                                    )
                                    Text(
                                        text = if (hasPermission) "Status: Granted (Active)" else "Status: Not Granted",
                                        fontSize = 12.sp,
                                        color = if (hasPermission) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (!hasPermission) {
                                Button(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            viewModel.updateNotificationPermission(true)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("request_permission_button")
                                ) {
                                    Text("Grant", fontSize = 11.sp)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MicdCardBorder)

                        // Median.co Bridge Diagnostics
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = MicdSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Median.co JS Bridge Integration:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MicdTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isMedianBridge)
                                "✓ Median.co runtime active. FCM bridge linked to https://micd-smart-campus.base44.app."
                            else
                                "ℹ Running in Native Android standalone environment. Native FCM push engine and all 5 Android notification channels are fully active.",
                            fontSize = 11.sp,
                            color = MicdTextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // FCM Token Preview
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MicdSurfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("FCM Device Token:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MicdPrimary)
                                    TextButton(
                                        onClick = { viewModel.refreshFcmToken() },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                    ) {
                                        Text("Rotate Token", fontSize = 10.sp)
                                    }
                                }
                                Text(
                                    text = if (fcmToken.isNotBlank()) fcmToken else "Generating token...",
                                    fontSize = 10.sp,
                                    color = MicdTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 2. Notification Preferences Section
            item {
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "अधिसूचना श्रेणियां एवं प्राथमिकताएं" else "Notification Categories & Channels",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MicdTextPrimary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Master Toggle
                        PreferenceToggleRow(
                            title = "Master Notifications",
                            subtitle = "Enable or pause all notification broadcasts",
                            checked = preferences.enableAll,
                            onCheckedChange = { viewModel.togglePreferenceCategory("enableAll", it) },
                            icon = Icons.Default.Notifications
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MicdCardBorder)

                        // 1. Announcements
                        PreferenceToggleRow(
                            title = "School Announcements",
                            subtitle = "Urgent alerts, holidays, Principal updates [school_announcements]",
                            checked = preferences.announcements && preferences.enableAll,
                            enabled = preferences.enableAll,
                            onCheckedChange = { viewModel.togglePreferenceCategory("announcements", it) },
                            icon = Icons.Default.Campaign
                        )

                        // 2. Homework Updates
                        PreferenceToggleRow(
                            title = "Homework & Assignments",
                            subtitle = "Daily tasks, teacher instructions & deadlines [homework_updates]",
                            checked = preferences.homework && preferences.enableAll,
                            enabled = preferences.enableAll,
                            onCheckedChange = { viewModel.togglePreferenceCategory("homework", it) },
                            icon = Icons.Default.MenuBook
                        )

                        // 3. Examination Notices
                        PreferenceToggleRow(
                            title = "Examination Notices",
                            subtitle = "Date sheets, results and report card alerts [examination_notices]",
                            checked = preferences.examination && preferences.enableAll,
                            enabled = preferences.enableAll,
                            onCheckedChange = { viewModel.togglePreferenceCategory("examination", it) },
                            icon = Icons.Default.Grade
                        )

                        // 4. Quiz Alerts
                        PreferenceToggleRow(
                            title = "Quiz Alerts",
                            subtitle = "New quizzes, approaching deadlines & results [quiz_notifications]",
                            checked = preferences.quizAlerts && preferences.enableAll,
                            enabled = preferences.enableAll,
                            onCheckedChange = { viewModel.togglePreferenceCategory("quizAlerts", it) },
                            icon = Icons.Default.Quiz
                        )

                        // 5. Competition Alerts
                        PreferenceToggleRow(
                            title = "Competition Alerts",
                            subtitle = "Science fairs, Olympiads, debate registrations [competition_notifications]",
                            checked = preferences.competitionAlerts && preferences.enableAll,
                            enabled = preferences.enableAll,
                            onCheckedChange = { viewModel.togglePreferenceCategory("competitionAlerts", it) },
                            icon = Icons.Default.EmojiEvents
                        )
                    }
                }
            }

            // 3. Notification History Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.HINDI) "सूचना इतिहास (History)" else "Notification Delivery History (${notifications.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MicdTextPrimary
                    )

                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAllNotifications() }) {
                            Text("Clear All", fontSize = 11.sp, color = MicdError)
                        }
                    }
                }
            }

            if (notifications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No delivered notifications yet.", fontSize = 12.sp, color = MicdTextSecondary)
                        }
                    }
                }
            } else {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (notif.channelId) {
                                        MedianNotificationManager.CHANNEL_EXAMINATION_NOTICES -> Color(0xFFE91E63).copy(alpha = 0.12f)
                                        MedianNotificationManager.CHANNEL_QUIZ_NOTIFICATIONS -> Color(0xFFED6C02).copy(alpha = 0.12f)
                                        MedianNotificationManager.CHANNEL_COMPETITION_NOTIFICATIONS -> Color(0xFF2E7D32).copy(alpha = 0.12f)
                                        else -> MicdPrimary.copy(alpha = 0.12f)
                                    }
                                ) {
                                    Text(
                                        text = notif.channelId.replace("_", " ").uppercase(),
                                        color = when (notif.channelId) {
                                            MedianNotificationManager.CHANNEL_EXAMINATION_NOTICES -> Color(0xFFE91E63)
                                            MedianNotificationManager.CHANNEL_QUIZ_NOTIFICATIONS -> Color(0xFFED6C02)
                                            MedianNotificationManager.CHANNEL_COMPETITION_NOTIFICATIONS -> Color(0xFF2E7D32)
                                            else -> MicdPrimary
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "Status: ${notif.status}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = notif.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MicdTextPrimary
                            )

                            Text(
                                text = notif.message,
                                fontSize = 11.sp,
                                color = MicdTextSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Target: ${notif.targetRole} • By: ${notif.senderName}",
                                    fontSize = 10.sp,
                                    color = MicdTextMuted
                                )

                                IconButton(
                                    onClick = { viewModel.deleteNotification(notif.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MicdError, modifier = Modifier.size(16.dp))
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
fun PreferenceToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled && checked) MicdPrimary else MicdTextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (enabled) MicdTextPrimary else MicdTextMuted
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MicdTextSecondary,
                    lineHeight = 14.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MicdPrimary
            )
        )
    }
}
