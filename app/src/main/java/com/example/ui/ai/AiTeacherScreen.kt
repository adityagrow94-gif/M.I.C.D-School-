package com.example.ui.ai

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.ai.AiLanguage
import com.example.data.model.AiChatMessageEntity
import com.example.ui.state.AppLanguage
import com.example.ui.state.SchoolViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTeacherScreen(
    viewModel: SchoolViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val messages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var selectedClassLevel by remember { mutableStateOf(10) }
    var selectedAiLanguage by remember { mutableStateOf(AiLanguage.ENGLISH) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Suggested questions chips
    val suggestedPrompts = remember(selectedClassLevel, selectedAiLanguage) {
        when (selectedAiLanguage) {
            AiLanguage.HINDI -> listOf(
                "न्यूटन के गति के तीनों नियम सरल हिन्दी में समझाएं",
                "द्विघात समीकरण 2x² + 5x - 3 = 0 का चरणबद्ध हल",
                "हिन्दी व्याकरण: संज्ञा और सर्वनाम के भेद उदाहरण सहित",
                "कक्षा $selectedClassLevel विज्ञान के 5 महत्वपूर्ण अभ्यास प्रश्न",
                "प्रकाश का परावर्तन और अपवर्तन नियम"
            )
            AiLanguage.HINGLISH -> listOf(
                "Trigonometry formulas & standard values table samjhao",
                "Chemical reactions ke types with balanced equations",
                "Active and Passive voice ke basic rules with examples",
                "Class $selectedClassLevel Science revision quiz (3 MCQs)",
                "Ohm's law and electric circuits easy explanation"
            )
            AiLanguage.ENGLISH -> listOf(
                "Explain Newton's Laws of Motion with real-world examples",
                "Solve quadratic equation 2x² + 5x - 3 = 0 step by step",
                "English Grammar: Rules of Active and Passive Voice",
                "Class $selectedClassLevel Science: Cell Biology & Organelles",
                "Generate 3 practice MCQs for Class $selectedClassLevel Mathematics"
            )
        }
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
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .testTag("ai_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = MicdGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "M.I.C.D AI TEACHER",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "Educational AI • Classes 1 to 12 • Bilingual",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Clear Chat Action
                        IconButton(
                            onClick = { viewModel.clearAiChat() },
                            modifier = Modifier.testTag("ai_clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Chat",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Language Selector & Class Selector Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Language mode pills
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AiLanguage.values().forEach { lang ->
                                val isSelected = selectedAiLanguage == lang
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedAiLanguage = lang },
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = when (lang) {
                                            AiLanguage.ENGLISH -> "English"
                                            AiLanguage.HINDI -> "हिन्दी"
                                            AiLanguage.HINGLISH -> "Hinglish"
                                        },
                                        color = if (isSelected) MicdPrimary else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Class Level Dropdown Pill
                        var classMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { classMenuExpanded = true },
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Class $selectedClassLevel",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = classMenuExpanded,
                                onDismissRequest = { classMenuExpanded = false }
                            ) {
                                (1..12).forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("Class $c") },
                                        onClick = {
                                            selectedClassLevel = c
                                            classMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Quick prompt suggestions row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(suggestedPrompts) { prompt ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.sendAiMessage(prompt, selectedClassLevel, selectedAiLanguage)
                                },
                            color = MicdSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MicdCardBorder),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                color = MicdPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input Box & Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (selectedAiLanguage == AiLanguage.HINDI)
                                    "कक्षा $selectedClassLevel का प्रश्न यहाँ लिखें..."
                                else
                                    "Ask Class $selectedClassLevel educational question...",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 100.dp)
                            .testTag("ai_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isThinking) {
                                val text = inputText
                                inputText = ""
                                viewModel.sendAiMessage(text, selectedClassLevel, selectedAiLanguage)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MicdPrimary, CircleShape)
                            .testTag("ai_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MicdBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Welcome Greeting Message if no messages
            if (messages.isEmpty()) {
                item {
                    AiWelcomeCard(
                        classLevel = selectedClassLevel,
                        language = selectedAiLanguage,
                        onPromptClick = { prompt ->
                            viewModel.sendAiMessage(prompt, selectedClassLevel, selectedAiLanguage)
                        }
                    )
                }
            }

            items(messages, key = { it.id }) { msg ->
                AiMessageBubble(message = msg)
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MicdPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "M.I.C.D AI Teacher is preparing step-by-step explanation...",
                            fontSize = 12.sp,
                            color = MicdTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiWelcomeCard(
    classLevel: Int,
    language: AiLanguage,
    onPromptClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MicdPrimary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MicdPrimary, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Namaste & Welcome!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MicdTextPrimary
                    )
                    Text(
                        text = "I am M.I.C.D AI TEACHER for Class $classLevel",
                        fontSize = 12.sp,
                        color = MicdSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "I help students and teachers across Classes 1 to 12 with:\n" +
                        "• Step-by-step Mathematics proofs & equations\n" +
                        "• Science chapter breakdowns (Physics, Chemistry, Biology)\n" +
                        "• Hindi & English grammar rules and translations\n" +
                        "• Practice quizzes, worksheets & exam revision",
                fontSize = 12.sp,
                color = MicdTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tap a sample question below to start:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MicdTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            listOf(
                "Solve quadratic equation 2x² + 5x - 3 = 0 step by step",
                "Explain Newton's Laws in simple Hindi",
                "English Grammar: Active and Passive Voice rules"
            ).forEach { sample ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPromptClick(sample) },
                    color = MicdSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MicdPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(sample, fontSize = 11.sp, color = MicdPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun AiMessageBubble(message: AiChatMessageEntity) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MicdPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            color = if (isUser) MicdPrimary else Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = if (isUser) Color.White else MicdTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
