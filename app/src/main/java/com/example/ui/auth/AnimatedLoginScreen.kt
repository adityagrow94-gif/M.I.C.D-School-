package com.example.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.state.AppLanguage
import com.example.ui.state.SchoolViewModel
import com.example.ui.state.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedLoginScreen(
    viewModel: SchoolViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Animation Transitions for the pulsating emblem and background halo
    val infiniteTransition = rememberInfiniteTransition(label = "halo_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Mode: 0 = Student Registration (पंजीकरण), 1 = Student Login (लॉगिन), 2 = Quick Demo
    var activeMode by remember { mutableStateOf(0) }

    // Student Registration Form Fields (Mandated by user request):
    // 1. student name
    // 2. roll number
    // 3. varg (class and section)
    // 4. mobile number
    // 5. parents' name
    // 6. password
    var studentNameInput by remember { mutableStateOf("") }
    var rollNumberInput by remember { mutableStateOf("") }
    var selectedClassInput by remember { mutableIntStateOf(10) }
    var selectedSectionInput by remember { mutableStateOf("A") } // varg
    var mobileNumberInput by remember { mutableStateOf("") }
    var parentsNameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A2647),
                        Color(0xFF144272),
                        Color(0xFF205295),
                        Color(0xFF2C74B3)
                    )
                )
            )
    ) {
        // Decorative Animated Rotating Aura in the background
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-40).dp)
                .rotate(rotationAngle)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x33FFD700),
                            Color(0x1A0D47A1),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.toggleLanguage() }
                        .testTag("auth_lang_toggle"),
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLang == AppLanguage.ENGLISH) "हिन्दी" else "EN",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated School Emblem & Branding
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(2.5.dp, MicdGold, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_micd_logo),
                    contentDescription = "M.I.C.D School Emblem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "M.I.C.D SCHOOL",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.2.sp
            )

            Text(
                text = if (currentLang == AppLanguage.HINDI)
                    "कक्षा 1 से 12 स्मार्ट कैम्पस पोर्टल"
                else
                    "Classes 1 to 12 • Smart AI Campus Portal",
                fontSize = 12.sp,
                color = MicdGold,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error / Success Banners
            errorMessage?.let { error ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFC62828))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(error, color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            successMessage?.let { success ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(success, color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Animated Mode Selector Tabs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Tab 0: New Registration
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                activeMode = 0
                                errorMessage = null
                            },
                        color = if (activeMode == 0) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "नया पंजीकरण" else "Register",
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeMode == 0) MicdPrimary else Color.White
                        )
                    }

                    // Tab 1: Login
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                activeMode = 1
                                errorMessage = null
                            },
                        color = if (activeMode == 1) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "लॉगिन करें" else "Sign In",
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeMode == 1) MicdPrimary else Color.White
                        )
                    }

                    // Tab 2: Quick Demo
                    Surface(
                        modifier = Modifier
                            .weight(0.9f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                activeMode = 2
                                errorMessage = null
                            },
                        color = if (activeMode == 2) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.HINDI) "डेमो प्रवेश" else "Demo",
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeMode == 2) MicdPrimary else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Auth Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeMode) {
                        0 -> {
                            // Student Registration Form
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "विद्यार्थी विवरण दर्ज करें" else "Enter Student Credentials",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MicdTextPrimary
                            )

                            // 1. Student Name
                            OutlinedTextField(
                                value = studentNameInput,
                                onValueChange = { studentNameInput = it },
                                label = { Text(if (currentLang == AppLanguage.HINDI) "विद्यार्थी का नाम (Student Name)" else "Student Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MicdPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_student_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // 2. Class and Varg (Section) selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedClassInput.toString(),
                                    onValueChange = { selectedClassInput = it.toIntOrNull() ?: selectedClassInput },
                                    label = { Text(if (currentLang == AppLanguage.HINDI) "कक्षा (1-12)" else "Class (1-12)") },
                                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = MicdPrimary) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reg_class_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = selectedSectionInput,
                                    onValueChange = { selectedSectionInput = it.uppercase() },
                                    label = { Text(if (currentLang == AppLanguage.HINDI) "वर्ग (A/B/C)" else "Varg (Section)") },
                                    leadingIcon = { Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = MicdPrimary) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reg_section_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // 3. Roll Number
                            OutlinedTextField(
                                value = rollNumberInput,
                                onValueChange = { rollNumberInput = it },
                                label = { Text(if (currentLang == AppLanguage.HINDI) "रोल नंबर (Roll No: 1 to 100)" else "Roll Number (1-100)") },
                                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = MicdPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_roll_number_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // 4. Mobile Number
                            OutlinedTextField(
                                value = mobileNumberInput,
                                onValueChange = { mobileNumberInput = it },
                                label = { Text(if (currentLang == AppLanguage.HINDI) "मोबाइल नंबर (Mobile Number)" else "Mobile Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MicdPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_mobile_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // 5. Parents' Name
                            OutlinedTextField(
                                value = parentsNameInput,
                                onValueChange = { parentsNameInput = it },
                                label = { Text(if (currentLang == AppLanguage.HINDI) "माता / पिता का नाम (Parents Name)" else "Father / Mother Name") },
                                leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = MicdPrimary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_parents_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // 6. Password
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text(if (currentLang == AppLanguage.HINDI) "पासवर्ड बनाएं (Password)" else "Set Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MicdPrimary) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_password_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Submit Button
                            Button(
                                onClick = {
                                    errorMessage = null
                                    val roll = rollNumberInput.toIntOrNull() ?: 1
                                    viewModel.registerStudentAccount(
                                        name = studentNameInput.trim(),
                                        rollNumber = roll,
                                        classNumber = selectedClassInput,
                                        section = selectedSectionInput.trim().ifBlank { "A" },
                                        mobileNumber = mobileNumberInput.trim(),
                                        parentsName = parentsNameInput.trim(),
                                        password = passwordInput.trim(),
                                        onSuccess = {
                                            successMessage = "पंजीकरण सफल! आपका स्वागत है。"
                                            onLoginSuccess()
                                        },
                                        onError = { error -> errorMessage = error }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_registration_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.HowToReg, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "खाता बनाएं एवं लॉगिन करें" else "Create Account & Sign In",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        1 -> {
                            // Student Sign In Form (By Class, Section/Varg, Roll & Password)
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "विद्यार्थी लॉगिन" else "Student Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MicdTextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedClassInput.toString(),
                                    onValueChange = { selectedClassInput = it.toIntOrNull() ?: selectedClassInput },
                                    label = { Text(if (currentLang == AppLanguage.HINDI) "कक्षा (1-12)" else "Class") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = selectedSectionInput,
                                    onValueChange = { selectedSectionInput = it.uppercase() },
                                    label = { Text(if (currentLang == AppLanguage.HINDI) "वर्ग (A/B/C)" else "Varg") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = rollNumberInput,
                                onValueChange = { rollNumberInput = it },
                                label = { Text(if (currentLang == AppLanguage.HINDI) "रोल नंबर (Roll Number)" else "Roll Number") },
                                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = MicdPrimary) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text(if (currentLang == AppLanguage.HINDI) "पासवर्ड (Password)" else "Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MicdPrimary) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    errorMessage = null
                                    val roll = rollNumberInput.toIntOrNull() ?: 1
                                    viewModel.loginStudentAccount(
                                        classNumber = selectedClassInput,
                                        section = selectedSectionInput.trim().ifBlank { "A" },
                                        rollNumber = roll,
                                        password = passwordInput.trim(),
                                        onSuccess = { onLoginSuccess() },
                                        onError = { error -> errorMessage = error }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("student_login_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "लॉगिन करें" else "Sign In",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        2 -> {
                            // Quick One-Tap Demo Access (Students, Teachers, Admin)
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "त्वरित डेमो लॉगिन" else "One-Tap Demo Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MicdTextPrimary
                            )

                            Text(
                                text = if (currentLang == AppLanguage.HINDI)
                                    "बिना पासवर्ड दर्ज किए सीधे विभिन्न पैनल का अवलोकन करें:"
                                else
                                    "Instantly explore any portal panel with pre-seeded accounts:",
                                fontSize = 12.sp,
                                color = MicdTextSecondary
                            )

                            // Student Demo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.switchRole(UserRole.STUDENT)
                                        onLoginSuccess()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Aryan Sharma (Class 10-A, Roll 1)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                        Text("विद्यार्थी पैनल • चैट, नोट्स व परीक्षा", fontSize = 11.sp, color = MicdTextSecondary)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2E7D32))
                                }
                            }

                            // Teacher Demo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.switchRole(UserRole.TEACHER)
                                        onLoginSuccess()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CastForEducation, contentDescription = null, tint = Color(0xFF0288D1))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Mr. Rajesh Sharma (Faculty)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0288D1))
                                        Text("शिक्षक पैनल • हाजिरी, वीडियो व गृहकार्य", fontSize = 11.sp, color = MicdTextSecondary)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF0288D1))
                                }
                            }

                            // Admin Demo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.switchRole(UserRole.ADMIN)
                                        onLoginSuccess()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFD32F2F))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Principal & Super Admin", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD32F2F))
                                        Text("प्रशासनिक केंद्र • संपूर्ण स्कूल प्रबंधन", fontSize = 11.sp, color = MicdTextSecondary)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFD32F2F))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer note
            Text(
                text = "M.I.C.D School © 2024-2025 • Empowered with AI Smart Education",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}
