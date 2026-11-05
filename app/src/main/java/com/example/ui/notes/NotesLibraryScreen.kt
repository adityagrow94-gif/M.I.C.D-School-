package com.example.ui.notes

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StudyNoteEntity
import com.example.ui.state.AppLanguage
import com.example.ui.state.AppStrings
import com.example.ui.state.SchoolViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesLibraryScreen(
    viewModel: SchoolViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val currentLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()

    var selectedClass by remember { mutableStateOf(10) }
    var selectedSubject by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var activeNoteForReader by remember { mutableStateOf<StudyNoteEntity?>(null) }

    val subjects = listOf("All", "Mathematics", "Science", "Hindi", "English", "Physics", "Chemistry", "Computer Science")

    val filteredNotes = remember(allNotes, selectedClass, selectedSubject, searchQuery) {
        allNotes.filter { note ->
            val matchClass = note.classNumber == selectedClass
            val matchSubject = selectedSubject == "All" || note.subject.equals(selectedSubject, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.chapterName.contains(searchQuery, ignoreCase = true) ||
                    note.topic.contains(searchQuery, ignoreCase = true)
            matchClass && matchSubject && matchQuery
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
                            Text(
                                text = if (currentLang == AppLanguage.HINDI) "एम.आई.सी.डी अध्ययन नोट्स लाइब्रेरी" else "Study Notes & Resource Library",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Classes 1-12 • Chapter-wise Notes & PDF Summaries",
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by chapter, topic or keyword...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MicdTextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Class Filter Horizontal Bar (1 to 12)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Class:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MicdTextSecondary, modifier = Modifier.width(46.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items((1..12).toList()) { c ->
                        FilterChip(
                            selected = selectedClass == c,
                            onClick = { selectedClass = c },
                            label = { Text("Class $c", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MicdPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subject Filter Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Subject:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MicdTextSecondary, modifier = Modifier.width(46.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(subjects) { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MicdSecondary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Class $selectedClass Notes (${filteredNotes.size} available)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MicdTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Notes List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeNoteForReader = note },
                        shape = RoundedCornerShape(14.dp),
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
                                    shape = RoundedCornerShape(4.dp),
                                    color = MicdPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${note.subject} • Chapter ${note.chapterNumber}",
                                        color = MicdPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.toggleBookmarkNote(note) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (note.isBookmarked) MicdGold else MicdTextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = note.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MicdTextPrimary
                            )

                            Text(
                                text = "Topic: ${note.topic}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MicdSecondary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = note.content.take(180) + if (note.content.length > 180) "..." else "",
                                fontSize = 11.sp,
                                color = MicdTextSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "By ${note.authorName} • ${note.dateAdded}",
                                    fontSize = 10.sp,
                                    color = MicdTextMuted
                                )

                                Button(
                                    onClick = { activeNoteForReader = note },
                                    colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Read Inside", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // In-App Note Reader Modal
    activeNoteForReader?.let { note ->
        AlertDialog(
            onDismissRequest = { activeNoteForReader = null },
            title = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MicdPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Class ${note.classNumber} • ${note.subject} • Chapter ${note.chapterNumber}",
                            color = MicdPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MicdTextPrimary)
                    Text(note.topic, fontSize = 12.sp, color = MicdSecondary)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Divider(color = MicdCardBorder)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note.content,
                            fontSize = 13.sp,
                            color = MicdTextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeNoteForReader = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MicdPrimary)
                ) {
                    Text("Close Reader")
                }
            }
        )
    }
}
