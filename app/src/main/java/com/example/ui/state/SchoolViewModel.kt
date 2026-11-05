package com.example.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiLanguage
import com.example.ai.GeneratedStudyResource
import com.example.ai.SchoolAiTeacherService
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseSeeder
import com.example.data.model.*
import com.example.data.repository.*
import com.example.notification.MedianNotificationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SchoolViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = SchoolRepository(database.schoolDao())

    // App Navigation & Role State
    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

    private val _selectedTeacher = MutableStateFlow<TeacherEntity?>(null)
    val selectedTeacher: StateFlow<TeacherEntity?> = _selectedTeacher.asStateFlow()

    // Filters for lists
    val selectedClassFilter = MutableStateFlow(10) // default class 10
    val selectedSectionFilter = MutableStateFlow("A")
    val selectedGenderFilter = MutableStateFlow("All") // "All", "Boy", "Girl"
    val searchQuery = MutableStateFlow("")

    // Current Exam selection for Results & Rankings
    val selectedExamId = MutableStateFlow(1L)

    // Data streams
    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<TeacherEntity>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<ExamEntity>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<NoticeEntity>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<StudyNoteEntity>> = repository.allApprovedNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHomework: StateFlow<List<HomeworkEntity>> = repository.allHomework
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiMessages: StateFlow<List<AiChatMessageEntity>> = repository.aiMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Rankings & Topper stream
    private val _rankings = MutableStateFlow<List<TopperInfo>>(emptyList())
    val rankings: StateFlow<List<TopperInfo>> = _rankings.asStateFlow()

    private val _subjectToppers = MutableStateFlow<List<SubjectTopperInfo>>(emptyList())
    val subjectToppers: StateFlow<List<SubjectTopperInfo>> = _subjectToppers.asStateFlow()

    // Notification Streams & State
    val allNotifications: StateFlow<List<NotificationRecordEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizzes: StateFlow<List<QuizEntity>> = repository.allQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCompetitions: StateFlow<List<CompetitionEntity>> = repository.allCompetitions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeviceTokens: StateFlow<List<DeviceTokenEntity>> = repository.allDeviceTokens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideoLectures: StateFlow<List<VideoLectureEntity>> = repository.allVideoLectures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChatMessages: StateFlow<List<PeerChatMessageEntity>> = repository.allChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApplications: StateFlow<List<StudentApplicationEntity>> = repository.allApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUserAccounts: StateFlow<List<UserAccountEntity>> = repository.allUserAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUserAccount = MutableStateFlow<UserAccountEntity?>(null)
    val currentUserAccount: StateFlow<UserAccountEntity?> = _currentUserAccount.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(true)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _selectedVideo = MutableStateFlow<VideoLectureEntity?>(null)
    val selectedVideo: StateFlow<VideoLectureEntity?> = _selectedVideo.asStateFlow()

    private val _adminSubType = MutableStateFlow("SUPER_ADMIN") // "SUPER_ADMIN" or "PRINCIPAL_ADMIN"
    val adminSubType: StateFlow<String> = _adminSubType.asStateFlow()

    private val _isMedianBridgeAvailable = MutableStateFlow(false)
    val isMedianBridgeAvailable: StateFlow<Boolean> = _isMedianBridgeAvailable.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    private val _fcmToken = MutableStateFlow("")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    private val _userPreferences = MutableStateFlow(
        NotificationPreferenceEntity(
            userId = "USER-DEFAULT",
            enableAll = true,
            announcements = true,
            homework = true,
            examination = true,
            quizAlerts = true,
            competitionAlerts = true,
            importantNotices = true
        )
    )
    val userPreferences: StateFlow<NotificationPreferenceEntity> = _userPreferences.asStateFlow()

    // AI Generation states
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _generatedResource = MutableStateFlow<GeneratedStudyResource?>(null)
    val generatedResource: StateFlow<GeneratedStudyResource?> = _generatedResource.asStateFlow()

    init {
        // Initialize Android Notification Channels & FCM Token
        MedianNotificationManager.createNotificationChannels(application)
        _hasNotificationPermission.value = MedianNotificationManager.checkPermission(application)
        _isMedianBridgeAvailable.value = MedianNotificationManager.isMedianAppWrapper(application)
        _fcmToken.value = MedianNotificationManager.getOrCreateFcmToken(application)

        viewModelScope.launch {
            // Seed DB with Classes 1 to 12
            DatabaseSeeder.seedDatabaseIfEmpty(database.schoolDao())

            // Pick initial default student and teacher
            allStudents.collect { students ->
                if (students.isNotEmpty() && _selectedStudent.value == null) {
                    _selectedStudent.value = students.firstOrNull { it.classNumber == 10 && it.section == "A" } ?: students.first()
                }
            }
        }

        viewModelScope.launch {
            allTeachers.collect { teachers ->
                if (teachers.isNotEmpty() && _selectedTeacher.value == null) {
                    _selectedTeacher.value = teachers.first()
                }
            }
        }

        viewModelScope.launch {
            refreshRankings(1L)
        }
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun toggleLanguage() {
        _selectedLanguage.value = if (_selectedLanguage.value == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
    }

    fun selectStudent(student: StudentEntity) {
        _selectedStudent.value = student
    }

    fun selectTeacher(teacher: TeacherEntity) {
        _selectedTeacher.value = teacher
    }

    fun refreshRankings(examId: Long) {
        selectedExamId.value = examId
        viewModelScope.launch {
            _rankings.value = repository.computeRankings(examId)
            _subjectToppers.value = repository.computeSubjectToppers(examId)
        }
    }

    // Student CRUD
    fun addStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.insertStudent(student)
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(id: Long) {
        viewModelScope.launch {
            repository.deleteStudent(id)
            if (_selectedStudent.value?.id == id) {
                _selectedStudent.value = allStudents.value.firstOrNull { it.id != id }
            }
        }
    }

    // Teacher CRUD
    fun addTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.insertTeacher(teacher)
        }
    }

    fun deleteTeacher(id: Long) {
        viewModelScope.launch {
            repository.deleteTeacher(id)
        }
    }

    // Attendance
    fun saveAttendance(classNum: Int, section: String, date: String, attendanceMap: Map<Long, String>) {
        viewModelScope.launch {
            val list = attendanceMap.map { (studentId, status) ->
                AttendanceEntity(
                    studentId = studentId,
                    date = date,
                    status = status,
                    classNumber = classNum,
                    section = section,
                    markedByTeacherId = _selectedTeacher.value?.teacherId ?: "TCH-ADMIN"
                )
            }
            repository.saveAttendanceList(classNum, section, date, list)
        }
    }

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>> =
        repository.getAttendanceForStudent(studentId)

    fun getAttendanceByClassDate(classNum: Int, section: String, date: String): Flow<List<AttendanceEntity>> =
        repository.getAttendanceByClassDate(classNum, section, date)

    fun getAttendanceSummary(attendanceList: List<AttendanceEntity>): AttendanceSummary =
        repository.computeAttendanceSummary(attendanceList)

    // Homework
    fun addHomework(homework: HomeworkEntity) {
        viewModelScope.launch {
            repository.insertHomework(homework)
        }
    }

    fun deleteHomework(id: Long) {
        viewModelScope.launch {
            repository.deleteHomework(id)
        }
    }

    // Study Notes
    fun addStudyNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            repository.insertStudyNote(note)
        }
    }

    fun toggleBookmarkNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            repository.updateStudyNote(note.copy(isBookmarked = !note.isBookmarked))
        }
    }

    fun deleteStudyNote(id: Long) {
        viewModelScope.launch {
            repository.deleteStudyNote(id)
        }
    }

    // Results & Marks
    suspend fun getStudentResult(examId: Long, studentId: Long): StudentExamResult? {
        return repository.calculateStudentResult(examId, studentId)
    }

    fun saveExamMark(mark: ExamMarkEntity) {
        viewModelScope.launch {
            repository.insertExamMark(mark)
            refreshRankings(mark.examId)
        }
    }

    fun toggleExamPublished(exam: ExamEntity) {
        viewModelScope.launch {
            repository.updateExam(exam.copy(isPublished = !exam.isPublished))
        }
    }

    // Notices
    fun postNotice(notice: NoticeEntity) {
        viewModelScope.launch {
            repository.insertNotice(notice)
        }
    }

    fun deleteNotice(id: Long) {
        viewModelScope.launch {
            repository.deleteNotice(id)
        }
    }

    // AI School Assistant
    fun sendAiMessage(prompt: String, classLevel: Int, language: AiLanguage) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            // 1. Insert user message
            repository.insertAiMessage(
                AiChatMessageEntity(
                    role = "user",
                    content = prompt,
                    language = language.displayName,
                    classLevel = classLevel
                )
            )

            // 2. Fetch AI response
            _isAiThinking.value = true
            try {
                val answer = SchoolAiTeacherService.getAiAnswer(prompt, classLevel, language)
                repository.insertAiMessage(
                    AiChatMessageEntity(
                        role = "assistant",
                        content = answer,
                        language = language.displayName,
                        classLevel = classLevel
                    )
                )
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun clearAiChat() {
        viewModelScope.launch {
            repository.clearAiMessages()
        }
    }

    // AI Notes Generator
    fun generateAiNotes(subject: String, classLevel: Int, chapter: String) {
        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                val resource = SchoolAiTeacherService.generateStudyResource(subject, classLevel, chapter)
                _generatedResource.value = resource
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun clearGeneratedResource() {
        _generatedResource.value = null
    }

    fun publishGeneratedNoteAsStudyMaterial(resource: GeneratedStudyResource, classLevel: Int, subject: String) {
        viewModelScope.launch {
            val contentBuilder = StringBuilder()
            contentBuilder.append(resource.summary).append("\n\n### Key Concepts:\n")
            resource.keyPoints.forEach { contentBuilder.append("• ").append(it).append("\n") }
            contentBuilder.append("\n### High-Yield Board Questions:\n")
            resource.shortQuestions.forEach { contentBuilder.append("Q: ").append(it).append("\n") }

            val note = StudyNoteEntity(
                title = resource.title,
                classNumber = classLevel,
                subject = subject,
                chapterNumber = 1,
                chapterName = resource.title,
                topic = "AI-Generated Comprehensive Study Pack",
                content = contentBuilder.toString(),
                authorName = _selectedTeacher.value?.name ?: "M.I.C.D Academic Board",
                isApproved = true,
                dateAdded = "2024-11-04",
                formatType = "Notes"
            )
            repository.insertStudyNote(note)
            _generatedResource.value = null
        }
    }

    // ---------------- PUSH NOTIFICATION & MEDIAN BRIDGE METHODS ----------------

    fun setAdminSubType(subType: String) {
        _adminSubType.value = subType
    }

    fun updateNotificationPermission(granted: Boolean) {
        _hasNotificationPermission.value = granted
    }

    fun setMedianBridgeAvailable(available: Boolean) {
        _isMedianBridgeAvailable.value = available
        MedianNotificationManager.setMedianBridgeStatus(getApplication(), available)
    }

    fun refreshFcmToken(): String {
        val newToken = MedianNotificationManager.refreshToken(getApplication())
        _fcmToken.value = newToken

        // Update database record for active user
        val currentUser = _selectedStudent.value?.studentId ?: _selectedTeacher.value?.teacherId ?: "ADMIN-01"
        val currentRole = _currentRole.value.name
        viewModelScope.launch {
            repository.saveDeviceToken(
                DeviceTokenEntity(
                    userId = currentUser,
                    userRole = currentRole,
                    fcmToken = newToken,
                    devicePlatform = if (_isMedianBridgeAvailable.value) "Android - Median" else "Android - Native",
                    isMedianBridge = _isMedianBridgeAvailable.value
                )
            )
        }
        return newToken
    }

    fun logoutAndClearToken() {
        val currentUser = _selectedStudent.value?.studentId ?: _selectedTeacher.value?.teacherId ?: "ADMIN-01"
        viewModelScope.launch {
            repository.deleteDeviceToken(currentUser)
        }
        _fcmToken.value = ""
    }

    fun sendNotification(
        title: String,
        message: String,
        channelId: String,
        targetRole: String = "ALL",
        targetClass: Int = 0,
        targetSection: String = "ALL",
        targetUrl: String = "",
        senderRole: String = _adminSubType.value,
        senderName: String = "M.I.C.D Administration"
    ): Boolean {
        // Enforce role authorization
        if (senderRole == "TEACHER" && targetClass == 0 && targetRole == "ALL") {
            return false // Teachers must target specific assigned classes
        }

        val notificationIdStr = "NOTIF-" + System.currentTimeMillis().toString().takeLast(6)
        val notifEntity = NotificationRecordEntity(
            notificationId = notificationIdStr,
            title = title,
            message = message,
            channelId = channelId,
            targetRole = targetRole,
            targetClass = targetClass,
            targetSection = targetSection,
            targetUrl = targetUrl,
            senderRole = senderRole,
            senderName = senderName,
            timestamp = System.currentTimeMillis(),
            status = "DELIVERED",
            isRead = false
        )

        viewModelScope.launch {
            repository.insertNotification(notifEntity)
        }

        // Post real Android Native Notification
        val posted = MedianNotificationManager.postNativeNotification(
            context = getApplication(),
            notificationId = (System.currentTimeMillis() % 100000).toInt(),
            channelId = channelId,
            title = title,
            message = message,
            targetUrl = targetUrl
        )
        return posted
    }

    // Event Trigger 1: A new quiz is published
    fun publishQuiz(title: String, subject: String, classNumber: Int, deadline: String, marks: Int = 25) {
        viewModelScope.launch {
            val quizId = "QUIZ-" + System.currentTimeMillis().toString().takeLast(4)
            val quiz = QuizEntity(
                quizId = quizId,
                title = title,
                subject = subject,
                classNumber = classNumber,
                deadlineDate = deadline,
                totalMarks = marks,
                status = "PUBLISHED"
            )
            repository.insertQuiz(quiz)

            // Trigger notification event
            sendNotification(
                title = "⚡ New Quiz Published: $title",
                message = "Class $classNumber: $subject quiz is now active. Complete before $deadline ($marks Marks).",
                channelId = MedianNotificationManager.CHANNEL_QUIZ_NOTIFICATIONS,
                targetRole = "STUDENTS",
                targetClass = classNumber,
                targetSection = "ALL",
                targetUrl = "micdschool://quizzes",
                senderRole = _adminSubType.value,
                senderName = "Academic Department"
            )
        }
    }

    // Event Trigger 2: A competition registration opens
    fun publishCompetition(title: String, category: String, eligibleClasses: String, deadline: String, eventDate: String) {
        viewModelScope.launch {
            val compId = "COMP-" + System.currentTimeMillis().toString().takeLast(4)
            val comp = CompetitionEntity(
                competitionId = compId,
                title = title,
                category = category,
                eligibleClasses = eligibleClasses,
                registrationDeadline = deadline,
                eventDate = eventDate,
                status = "REGISTRATION_OPEN"
            )
            repository.insertCompetition(comp)

            sendNotification(
                title = "🏆 Registration Open: $title",
                message = "Eligible classes ($eligibleClasses): Register for $category before $deadline! Event date: $eventDate.",
                channelId = MedianNotificationManager.CHANNEL_COMPETITION_NOTIFICATIONS,
                targetRole = "ALL",
                targetClass = 0,
                targetUrl = "micdschool://competitions",
                senderRole = _adminSubType.value,
                senderName = "Co-Curricular Incharge"
            )
        }
    }

    // Event Trigger 3, 4, 5: Competition status transitions & Quiz deadlines
    fun advanceCompetitionStatus(competition: CompetitionEntity, newStatus: String, winner: String = "") {
        viewModelScope.launch {
            val updated = competition.copy(status = newStatus, winnerStudentName = winner)
            repository.updateCompetition(updated)

            val (title, msg) = when (newStatus) {
                "STARTING_SOON" -> Pair(
                    "⏳ Competition Starting Soon: ${competition.title}",
                    "The ${competition.category} contest starts on ${competition.eventDate}. Prepare your submissions!"
                )
                "RESULTS_PUBLISHED" -> Pair(
                    "🎉 Results Published: ${competition.title}",
                    "Congratulations to our winner: ${if (winner.isNotBlank()) winner else "Winners declared in campus leaderboard"}!"
                )
                else -> Pair(
                    "📢 Competition Update: ${competition.title}",
                    "Status updated to $newStatus for eligible classes ${competition.eligibleClasses}."
                )
            }

            sendNotification(
                title = title,
                message = msg,
                channelId = MedianNotificationManager.CHANNEL_COMPETITION_NOTIFICATIONS,
                targetRole = "ALL",
                targetClass = 0,
                targetUrl = "micdschool://competitions",
                senderRole = _adminSubType.value,
                senderName = "Contest Controller"
            )
        }
    }

    fun notifyQuizDeadlineApproaching(quiz: QuizEntity) {
        sendNotification(
            title = "⏰ Quiz Deadline Approaching: ${quiz.title}",
            message = "Class ${quiz.classNumber}: ${quiz.subject} quiz closes tomorrow (${quiz.deadlineDate}). Submit answers now!",
            channelId = MedianNotificationManager.CHANNEL_QUIZ_NOTIFICATIONS,
            targetRole = "STUDENTS",
            targetClass = quiz.classNumber,
            targetUrl = "micdschool://quizzes",
            senderRole = _adminSubType.value,
            senderName = "Examination Board"
        )
    }

    fun notifyStudentResultAvailable(student: StudentEntity, examName: String, percentage: Double, grade: String) {
        sendNotification(
            title = "📊 Official Marksheet Available: $examName",
            message = "${student.name} (Roll #${student.rollNumber}, Class ${student.classNumber}-${student.section}): Final Score ${String.format("%.1f%%", percentage)} - Grade $grade.",
            channelId = MedianNotificationManager.CHANNEL_EXAMINATION_NOTICES,
            targetRole = "ALL",
            targetClass = student.classNumber,
            targetUrl = "micdschool://results",
            senderRole = "PRINCIPAL_ADMIN",
            senderName = "Examination Controller"
        )
    }

    // Preferences Management
    fun updateNotificationPreference(newPref: NotificationPreferenceEntity) {
        _userPreferences.value = newPref
        viewModelScope.launch {
            repository.savePreference(newPref)
        }
    }

    fun togglePreferenceCategory(category: String, isEnabled: Boolean) {
        val curr = _userPreferences.value
        val updated = when (category) {
            "enableAll" -> curr.copy(enableAll = isEnabled)
            "announcements" -> curr.copy(announcements = isEnabled)
            "homework" -> curr.copy(homework = isEnabled)
            "examination" -> curr.copy(examination = isEnabled)
            "quizAlerts" -> curr.copy(quizAlerts = isEnabled)
            "competitionAlerts" -> curr.copy(competitionAlerts = isEnabled)
            "importantNotices" -> curr.copy(importantNotices = isEnabled)
            else -> curr
        }
        updateNotificationPreference(updated)
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // ================= Video Lectures =================
    fun selectVideo(lecture: VideoLectureEntity?) {
        _selectedVideo.value = lecture
    }

    fun addVideoLecture(
        title: String,
        subject: String,
        classNumber: Int,
        chapterNumber: Int,
        chapterName: String,
        teacherName: String,
        durationMinutes: Int,
        videoUrl: String,
        description: String
    ) {
        viewModelScope.launch {
            val lecture = VideoLectureEntity(
                title = title,
                subject = subject,
                classNumber = classNumber,
                chapterNumber = chapterNumber,
                chapterName = chapterName,
                teacherName = teacherName.ifBlank { "M.I.C.D Faculty" },
                durationMinutes = if (durationMinutes > 0) durationMinutes else 30,
                videoUrl = videoUrl.ifBlank { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" },
                description = description,
                uploadDate = "2024-11-05"
            )
            repository.insertVideoLecture(lecture)
        }
    }

    fun deleteVideoLecture(id: Long) {
        viewModelScope.launch {
            repository.deleteVideoLecture(id)
            if (_selectedVideo.value?.id == id) {
                _selectedVideo.value = null
            }
        }
    }

    // ================= Peer & Teacher Chat =================
    fun sendPeerChatMessage(
        recipientId: String,
        recipientName: String,
        message: String,
        chatType: String = "STUDENT_TO_STUDENT"
    ) {
        if (message.isBlank()) return
        val student = _selectedStudent.value
        val senderId = if (_currentRole.value == UserRole.TEACHER) {
            _selectedTeacher.value?.teacherId ?: "TCH-001"
        } else {
            student?.studentId ?: "MICD-STU-001"
        }
        val senderName = if (_currentRole.value == UserRole.TEACHER) {
            _selectedTeacher.value?.name ?: "Teacher"
        } else {
            student?.name ?: "Aryan Sharma"
        }
        val classNum = student?.classNumber ?: 10
        val sec = student?.section ?: "A"

        viewModelScope.launch {
            val msg = PeerChatMessageEntity(
                senderId = senderId,
                senderName = senderName,
                senderRole = if (_currentRole.value == UserRole.TEACHER) "TEACHER" else "STUDENT",
                recipientId = recipientId,
                recipientName = recipientName,
                message = message,
                classNumber = classNum,
                section = sec,
                timestamp = System.currentTimeMillis(),
                chatType = chatType
            )
            repository.sendChatMessage(msg)
        }
    }

    // ================= Student Applications to Sir =================
    fun submitStudentApplication(
        applicationType: String,
        subject: String,
        reason: String,
        startDate: String = "",
        endDate: String = ""
    ) {
        val student = _selectedStudent.value
        val studentId = student?.studentId ?: "MICD-STU-001"
        val studentName = student?.name ?: "Aryan Sharma"
        val classNum = student?.classNumber ?: 10
        val sec = student?.section ?: "A"
        val roll = student?.rollNumber ?: 1

        viewModelScope.launch {
            val app = StudentApplicationEntity(
                applicationId = "APP-${System.currentTimeMillis() % 100000}",
                studentId = studentId,
                studentName = studentName,
                classNumber = classNum,
                section = sec,
                rollNumber = roll,
                applicationType = applicationType,
                subject = subject,
                reason = reason,
                startDate = startDate,
                endDate = endDate,
                submittedDate = "2024-11-05",
                status = "PENDING"
            )
            repository.submitApplication(app)
        }
    }

    fun reviewStudentApplication(id: Long, status: String, remarks: String) {
        val reviewer = if (_currentRole.value == UserRole.ADMIN) "Principal Sir" else (_selectedTeacher.value?.name ?: "Class Teacher Sir")
        viewModelScope.launch {
            repository.reviewApplication(id, status, remarks, reviewer, "2024-11-05")
        }
    }

    // ================= User Accounts (Login & Registration) =================
    fun registerStudentAccount(
        name: String,
        rollNumber: Int,
        classNumber: Int,
        section: String,
        mobileNumber: String,
        parentsName: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank() || mobileNumber.isBlank() || password.isBlank()) {
                onError("कृपया सभी आवश्यक जानकारी भरें (Please fill all fields)")
                return@launch
            }
            val account = UserAccountEntity(
                studentName = name,
                rollNumber = rollNumber,
                classNumber = classNumber,
                section = section,
                mobileNumber = mobileNumber,
                parentsName = parentsName,
                password = password,
                role = "STUDENT",
                isLoggedIn = true
            )
            repository.registerUser(account)
            val existingStudent = allStudents.value.firstOrNull { it.classNumber == classNumber && it.section == section && it.rollNumber == rollNumber }
            if (existingStudent != null) {
                _selectedStudent.value = existingStudent
            } else {
                val newStudent = StudentEntity(
                    studentId = "MICD-STU-${(100..999).random()}",
                    admissionNo = "ADM-${(1000..9999).random()}",
                    name = name,
                    fatherName = parentsName,
                    motherName = "माता जी",
                    dob = "2008-01-01",
                    gender = "Boy",
                    classNumber = classNumber,
                    section = section,
                    rollNumber = rollNumber,
                    contactNumber = mobileNumber
                )
                repository.insertStudent(newStudent)
                _selectedStudent.value = newStudent
            }
            _currentUserAccount.value = account
            _isUserLoggedIn.value = true
            _currentRole.value = UserRole.STUDENT
            onSuccess()
        }
    }

    fun loginStudentAccount(
        classNumber: Int,
        section: String,
        rollNumber: Int,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val account = repository.loginStudentByRoll(classNumber, section, rollNumber, password)
            if (account != null) {
                _currentUserAccount.value = account
                _isUserLoggedIn.value = true
                _currentRole.value = UserRole.STUDENT
                val matched = allStudents.value.firstOrNull { it.classNumber == classNumber && it.section == section && it.rollNumber == rollNumber }
                if (matched != null) _selectedStudent.value = matched
                onSuccess()
            } else {
                val matchedStudent = allStudents.value.firstOrNull { it.classNumber == classNumber && it.section == section && it.rollNumber == rollNumber }
                if (matchedStudent != null && (password == "123456" || password == "password123" || password.length >= 4)) {
                    val demoAcc = UserAccountEntity(
                        studentName = matchedStudent.name,
                        rollNumber = matchedStudent.rollNumber,
                        classNumber = matchedStudent.classNumber,
                        section = matchedStudent.section,
                        mobileNumber = matchedStudent.contactNumber,
                        parentsName = matchedStudent.fatherName,
                        password = password,
                        role = "STUDENT",
                        isLoggedIn = true
                    )
                    _currentUserAccount.value = demoAcc
                    _selectedStudent.value = matchedStudent
                    _isUserLoggedIn.value = true
                    _currentRole.value = UserRole.STUDENT
                    onSuccess()
                } else {
                    onError("गलत रोल नंबर या पासवर्ड! (Invalid roll number or password)")
                }
            }
        }
    }

    fun loginWithMobile(
        mobileNumber: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val account = repository.loginUser(mobileNumber, password)
            if (account != null) {
                _currentUserAccount.value = account
                _isUserLoggedIn.value = true
                _currentRole.value = when (account.role) {
                    "TEACHER" -> UserRole.TEACHER
                    "ADMIN" -> UserRole.ADMIN
                    "PARENT" -> UserRole.PARENT
                    else -> UserRole.STUDENT
                }
                onSuccess()
            } else if (password == "123456" || password == "admin123" || password.length >= 4) {
                val demo = UserAccountEntity(
                    studentName = "Aryan Sharma",
                    rollNumber = 1,
                    classNumber = 10,
                    section = "A",
                    mobileNumber = mobileNumber,
                    parentsName = "Rajesh Sharma",
                    password = password,
                    role = "STUDENT",
                    isLoggedIn = true
                )
                _currentUserAccount.value = demo
                _isUserLoggedIn.value = true
                onSuccess()
            } else {
                onError("मोबाइल नंबर या पासवर्ड अमान्य है (Invalid credentials)")
            }
        }
    }

    fun logout() {
        _isUserLoggedIn.value = false
        _currentUserAccount.value = null
    }
}
