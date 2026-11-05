package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.StudentEntity
import com.example.data.model.TeacherEntity
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.state.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolTopBar(
    currentRole: UserRole,
    currentLang: AppLanguage,
    onToggleLang: () -> Unit,
    onOpenRoleSwitcher: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenLogin: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // School Logo & Brand Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.5.dp, MicdGold, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_micd_logo),
                            contentDescription = "M.I.C.D School Emblem",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "एम.आई.सी.डी. स्कूल" else "M.I.C.D. School",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (currentRole) {
                                    UserRole.ADMIN -> Color(0xFFD32F2F)
                                    UserRole.TEACHER -> Color(0xFF0288D1)
                                    UserRole.STUDENT -> Color(0xFF2E7D32)
                                    UserRole.PARENT -> Color(0xFFE65100)
                                }
                            ) {
                                Text(
                                    text = AppStrings.roleLabel(currentRole, currentLang).uppercase(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "कक्षा 1 से 12" else "Classes 1–12",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Compact Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Switch Pill
                    Surface(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .clickable { onToggleLang() }
                            .testTag("lang_toggle_button"),
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(17.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentLang == AppLanguage.ENGLISH) "हिन्दी" else "EN",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Push Notification Settings & History Bell Button
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(alpha = 0.18f), CircleShape)
                            .testTag("notifications_bell_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Role Switcher Button
                    IconButton(
                        onClick = onOpenRoleSwitcher,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(alpha = 0.18f), CircleShape)
                            .testTag("switch_role_icon_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = "Switch Panel",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Login / Account Registration Button
                    IconButton(
                        onClick = onOpenLogin,
                        modifier = Modifier
                            .size(34.dp)
                            .background(MicdGold, CircleShape)
                            .testTag("auth_login_topbar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Login & Registration",
                            tint = Color(0xFF1A2332),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MicdTextPrimary
            )

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MicdTextSecondary
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FilterChipRow(
    selectedClass: Int,
    onClassSelected: (Int) -> Unit,
    selectedSection: String,
    onSectionSelected: (String) -> Unit,
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    currentLang: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, MicdCardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Gender Filter (Boys / Girls / All)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentLang == AppLanguage.HINDI) "लिंग:" else "Gender:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MicdTextSecondary,
                modifier = Modifier.width(60.dp)
            )

            listOf("All", "Boy", "Girl").forEach { gender ->
                val label = when (gender) {
                    "Boy" -> AppStrings.filterBoys(currentLang)
                    "Girl" -> AppStrings.filterGirls(currentLang)
                    else -> AppStrings.filterAll(currentLang)
                }
                val isSelected = selectedGender == gender
                FilterChip(
                    selected = isSelected,
                    onClick = { onGenderSelected(gender) },
                    label = { Text(label, fontSize = 11.sp) },
                    modifier = Modifier.padding(end = 6.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (gender == "Girl") Color(0xFFE91E63) else MicdPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Section Filter (A, B, C)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentLang == AppLanguage.HINDI) "सेक्शन:" else "Section:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MicdTextSecondary,
                modifier = Modifier.width(60.dp)
            )

            listOf("A", "B", "C").forEach { sec ->
                val isSelected = selectedSection == sec
                FilterChip(
                    selected = isSelected,
                    onClick = { onSectionSelected(sec) },
                    label = { Text("Sec $sec", fontSize = 11.sp) },
                    modifier = Modifier.padding(end = 6.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MicdSecondary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Class Filter (1 to 12)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentLang == AppLanguage.HINDI) "कक्षा:" else "Class:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MicdTextSecondary,
                modifier = Modifier.width(60.dp)
            )

            ScrollableTabRow(
                selectedTabIndex = (selectedClass - 1).coerceIn(0, 11),
                edgePadding = 0.dp,
                modifier = Modifier.weight(1f),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                (1..12).forEach { c ->
                    val isSelected = selectedClass == c
                    Tab(
                        selected = isSelected,
                        onClick = { onClassSelected(c) },
                        text = {
                            Text(
                                text = "Class $c",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MicdPrimary else MicdTextSecondary
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RoleSwitcherDialog(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    students: List<StudentEntity>,
    selectedStudent: StudentEntity?,
    onStudentSelected: (StudentEntity) -> Unit,
    teachers: List<TeacherEntity>,
    selectedTeacher: TeacherEntity?,
    onTeacherSelected: (TeacherEntity) -> Unit,
    currentLang: AppLanguage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, tint = MicdPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLang == AppLanguage.HINDI) "भूमिका एवं पैनल चयन" else "Switch School Panel",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (currentLang == AppLanguage.HINDI)
                        "M.I.C.D स्कूल के किसी भी पैनल को तुरंत जांचने के लिए चयन करें:"
                    else
                        "Select any authorized school panel to test real functionality:",
                    fontSize = 13.sp,
                    color = MicdTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Four role cards
                val roles = listOf(
                    Triple(UserRole.ADMIN, "Authorized Full School Control", Icons.Default.AdminPanelSettings),
                    Triple(UserRole.TEACHER, "Attendance, Homework, Marks & Notes", Icons.Default.CastForEducation),
                    Triple(UserRole.STUDENT, "Student Academic Record, AI & Notes", Icons.Default.Person),
                    Triple(UserRole.PARENT, "Ward's Performance, Attendance & Notices", Icons.Default.FamilyRestroom)
                )

                roles.forEach { (role, desc, icon) ->
                    val isSelected = currentRole == role
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onRoleSelected(role)
                                onDismiss()
                            }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MicdPrimary else MicdCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .testTag("role_${role.name.lowercase()}"),
                        color = if (isSelected) MicdPrimary.copy(alpha = 0.08f) else Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isSelected) MicdPrimary else Color(0xFFEEF2F8),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MicdPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.roleLabel(role, currentLang) + " Panel",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MicdTextPrimary
                                )
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = MicdTextSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = MicdPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLang == AppLanguage.HINDI) "बंद करें" else "Close")
            }
        }
    )
}
