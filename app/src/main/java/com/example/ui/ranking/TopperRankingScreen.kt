package com.example.ui.ranking

import androidx.activity.compose.BackHandler
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
import com.example.data.repository.SubjectTopperInfo
import com.example.data.repository.TopperInfo
import com.example.ui.state.AppLanguage
import com.example.ui.state.SchoolViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopperRankingScreen(
    viewModel: SchoolViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val rankings by viewModel.rankings.collectAsStateWithLifecycle()
    val subjectToppers by viewModel.subjectToppers.collectAsStateWithLifecycle()

    var activeViewTab by remember { mutableStateOf(0) } // 0: School Top 10 & Podium, 1: Class-wise 1-12 Toppers, 2: Boys vs Girls, 3: Subject Toppers
    var genderFilter by remember { mutableStateOf("All") } // "All", "Boy", "Girl"

    val displayRankings = remember(rankings, genderFilter) {
        if (genderFilter == "All") rankings
        else rankings.filter { it.student.gender == genderFilter }
    }

    // Class 1 to 12 toppers (highest percentage in each class)
    val classToppersMap = remember(rankings) {
        (1..12).associateWith { c ->
            rankings.filter { it.student.classNumber == c }.maxByOrNull { it.percentage }
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MicdGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.HINDI) "एम.आई.सी.डी टॉपर्स एवं मेधावी सूची" else "Toppers & Academic Rankings",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Classes 1 to 12 • Half-Yearly Board Examinations 2024",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MicdBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Sub Tabs (Top Podium, Class 1-12 Toppers, Gender Rankings, Subject Toppers)
            val tabs = listOf(
                "School Top 10",
                "Class 1-12 Toppers",
                "Boys / Girls",
                "Subject Toppers"
            )

            ScrollableTabRow(
                selectedTabIndex = activeViewTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = activeViewTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { activeViewTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MicdPrimary else MicdTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeViewTab) {
                0 -> {
                    // School Top 10 & Podium
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Top 3 Podium Card
                        item {
                            PodiumCard(topThree = rankings.take(3))
                        }

                        item {
                            Text(
                                text = "Top Rankers Leaderboard (Half-Yearly Examination)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MicdTextPrimary
                            )
                        }

                        items(rankings.take(20)) { item ->
                            RankingListItem(info = item)
                        }
                    }
                }

                1 -> {
                    // Class 1 to 12 Toppers
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items((1..12).toList()) { classNum ->
                            val topper = classToppersMap[classNum]
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
                                            .size(46.dp)
                                            .background(MicdGold.copy(alpha = 0.15f), CircleShape)
                                            .border(1.5.dp, MicdGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "C$classNum",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFFE65100)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Class $classNum Topper",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MicdSecondary
                                        )
                                        Text(
                                            text = topper?.student?.name ?: "Pending Evaluation",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MicdTextPrimary
                                        )
                                        Text(
                                            text = "Roll #${topper?.student?.rollNumber ?: 1} • Sec ${topper?.student?.section ?: "A"} • ${topper?.student?.gender ?: "Student"}",
                                            fontSize = 11.sp,
                                            color = MicdTextSecondary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (topper != null) String.format("%.1f%%", topper.percentage) else "-",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = if (topper != null) "${topper.totalObtained}/${topper.maxMarks}" else "",
                                            fontSize = 11.sp,
                                            color = MicdTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Boys vs Girls Rankings
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = genderFilter == "All",
                                onClick = { genderFilter = "All" },
                                label = { Text("All Students") }
                            )
                            FilterChip(
                                selected = genderFilter == "Boy",
                                onClick = { genderFilter = "Boy" },
                                label = { Text("Boys Ranking") }
                            )
                            FilterChip(
                                selected = genderFilter == "Girl",
                                onClick = { genderFilter = "Girl" },
                                label = { Text("Girls Ranking") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(displayRankings) { item ->
                                RankingListItem(info = item)
                            }
                        }
                    }
                }

                3 -> {
                    // Subject Toppers
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(subjectToppers) { subj ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(MicdPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = MicdPrimary)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(subj.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MicdPrimary)
                                        Text(
                                            text = "${subj.student.name} (Class ${subj.student.classNumber}-${subj.student.section})",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MicdTextPrimary
                                        )
                                        Text("Roll #${subj.student.rollNumber} • Adm: ${subj.student.admissionNo}", fontSize = 11.sp, color = MicdTextSecondary)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF2E7D32).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${subj.marksObtained} / ${subj.maxMarks}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

@Composable
fun PodiumCard(topThree: List<TopperInfo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 M.I.C.D SCHOOL TOPPERS PODIUM 🏆",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE65100),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place
                topThree.getOrNull(1)?.let { second ->
                    PodiumPillar(
                        rank = 2,
                        info = second,
                        color = Color(0xFF90A4AE),
                        height = 100.dp
                    )
                }

                // 1st Place (Center, tallest)
                topThree.getOrNull(0)?.let { first ->
                    PodiumPillar(
                        rank = 1,
                        info = first,
                        color = MicdGold,
                        height = 125.dp
                    )
                }

                // 3rd Place
                topThree.getOrNull(2)?.let { third ->
                    PodiumPillar(
                        rank = 3,
                        info = third,
                        color = Color(0xFFBCAAA4),
                        height = 80.dp
                    )
                }
            }
        }
    }
}

@Composable
fun PodiumPillar(
    rank: Int,
    info: TopperInfo,
    color: Color,
    height: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(95.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = info.student.name.split(" ").firstOrNull() ?: info.student.name,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MicdTextPrimary,
            maxLines = 1
        )
        Text(
            text = "Class ${info.student.classNumber}",
            fontSize = 10.sp,
            color = MicdTextSecondary
        )
        Text(
            text = String.format("%.1f%%", info.percentage),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF2E7D32)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (rank) {
                    1 -> "🥇 1ST"
                    2 -> "🥈 2ND"
                    else -> "🥉 3RD"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MicdTextPrimary
            )
        }
    }
}

@Composable
fun RankingListItem(info: TopperInfo) {
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
                    .background(
                        when (info.rank) {
                            1 -> MicdGold
                            2 -> Color(0xFF90A4AE)
                            3 -> Color(0xFFBCAAA4)
                            else -> MicdPrimary.copy(alpha = 0.1f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${info.rank}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (info.rank <= 3) Color.White else MicdPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(info.student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MicdTextPrimary)
                Text(
                    text = "Class ${info.student.classNumber}-${info.student.section} • Roll #${info.student.rollNumber} • ${info.student.gender}",
                    fontSize = 11.sp,
                    color = MicdTextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.1f%%", info.percentage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Grade ${info.grade}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MicdTextMuted
                )
            }
        }
    }
}
