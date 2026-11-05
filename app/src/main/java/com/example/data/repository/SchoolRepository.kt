package com.example.data.repository

import com.example.data.local.SchoolDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

data class StudentExamResult(
    val student: StudentEntity,
    val exam: ExamEntity,
    val subjectMarks: List<ExamMarkEntity>,
    val totalObtained: Int,
    val maxMarks: Int,
    val percentage: Double,
    val grade: String,
    val isPassed: Boolean,
    val classRank: Int = 0,
    val schoolRank: Int = 0,
    val remarks: String = ""
)

data class TopperInfo(
    val student: StudentEntity,
    val totalObtained: Int,
    val maxMarks: Int,
    val percentage: Double,
    val rank: Int,
    val grade: String
)

data class SubjectTopperInfo(
    val subject: String,
    val student: StudentEntity,
    val marksObtained: Int,
    val maxMarks: Int
)

data class AttendanceSummary(
    val totalDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val leaveDays: Int,
    val percentage: Double
)

class SchoolRepository(private val dao: SchoolDao) {
    // Basic reactive streams
    val allStudents: Flow<List<StudentEntity>> = dao.getAllStudents()
    val allTeachers: Flow<List<TeacherEntity>> = dao.getAllTeachers()
    val allExams: Flow<List<ExamEntity>> = dao.getAllExams()
    val allNotices: Flow<List<NoticeEntity>> = dao.getAllNotices()
    val allApprovedNotes: Flow<List<StudyNoteEntity>> = dao.getAllApprovedNotes()
    val allNotesAdmin: Flow<List<StudyNoteEntity>> = dao.getAllNotesAdmin()
    val allHomework: Flow<List<HomeworkEntity>> = dao.getAllHomework()
    val aiMessages: Flow<List<AiChatMessageEntity>> = dao.getAllAiMessages()
    val allNotifications: Flow<List<NotificationRecordEntity>> = dao.getAllNotifications()
    val allQuizzes: Flow<List<QuizEntity>> = dao.getAllQuizzes()
    val allCompetitions: Flow<List<CompetitionEntity>> = dao.getAllCompetitions()
    val allDeviceTokens: Flow<List<DeviceTokenEntity>> = dao.getAllDeviceTokens()
    val allVideoLectures: Flow<List<VideoLectureEntity>> = dao.getAllVideoLectures()
    val allChatMessages: Flow<List<PeerChatMessageEntity>> = dao.getAllChatMessages()
    val allApplications: Flow<List<StudentApplicationEntity>> = dao.getAllApplications()
    val allUserAccounts: Flow<List<UserAccountEntity>> = dao.getAllUserAccounts()

    fun getStudentsByClassAndSection(classNum: Int, section: String): Flow<List<StudentEntity>> =
        dao.getStudentsByClassAndSection(classNum, section)

    fun getStudentsByClass(classNum: Int): Flow<List<StudentEntity>> =
        dao.getStudentsByClass(classNum)

    suspend fun getStudentById(id: Long): StudentEntity? = dao.getStudentById(id)
    suspend fun getTeacherById(id: Long): TeacherEntity? = dao.getTeacherById(id)

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>> =
        dao.getAttendanceForStudent(studentId)

    fun getAttendanceByClassDate(classNum: Int, section: String, date: String): Flow<List<AttendanceEntity>> =
        dao.getAttendanceByClassDate(classNum, section, date)

    fun getHomeworkForClass(classNum: Int, section: String): Flow<List<HomeworkEntity>> =
        dao.getHomeworkForClass(classNum, section)

    fun getNotesByClass(classNum: Int): Flow<List<StudyNoteEntity>> =
        dao.getNotesByClass(classNum)

    fun getTimetableForClass(classNum: Int, section: String): Flow<List<TimetableEntity>> =
        dao.getTimetableForClass(classNum, section)

    fun getMarksForStudentExam(examId: Long, studentId: Long): Flow<List<ExamMarkEntity>> =
        dao.getMarksForStudentExam(examId, studentId)

    // Writes & Mutations
    suspend fun insertStudent(student: StudentEntity): Long = dao.insertStudent(student)
    suspend fun updateStudent(student: StudentEntity) = dao.updateStudent(student)
    suspend fun deleteStudent(id: Long) = dao.deleteStudentById(id)

    suspend fun insertTeacher(teacher: TeacherEntity): Long = dao.insertTeacher(teacher)
    suspend fun updateTeacher(teacher: TeacherEntity) = dao.updateTeacher(teacher)
    suspend fun deleteTeacher(id: Long) = dao.deleteTeacherById(id)

    suspend fun saveAttendanceList(classNum: Int, section: String, date: String, list: List<AttendanceEntity>) {
        dao.clearAttendanceForDate(classNum, section, date)
        dao.insertAttendance(list)
    }

    suspend fun insertHomework(homework: HomeworkEntity): Long = dao.insertHomework(homework)
    suspend fun deleteHomework(id: Long) = dao.deleteHomework(id)

    suspend fun insertStudyNote(note: StudyNoteEntity): Long = dao.insertNote(note)
    suspend fun updateStudyNote(note: StudyNoteEntity) = dao.updateNote(note)
    suspend fun deleteStudyNote(id: Long) = dao.deleteNote(id)

    suspend fun insertExam(exam: ExamEntity): Long = dao.insertExam(exam)
    suspend fun updateExam(exam: ExamEntity) = dao.updateExam(exam)
    suspend fun insertExamMark(mark: ExamMarkEntity): Long = dao.insertExamMark(mark)

    suspend fun insertNotice(notice: NoticeEntity): Long = dao.insertNotice(notice)
    suspend fun deleteNotice(id: Long) = dao.deleteNotice(id)

    suspend fun insertAiMessage(message: AiChatMessageEntity): Long = dao.insertAiMessage(message)
    suspend fun clearAiMessages() = dao.clearAiMessages()

    // Push Notifications
    fun getNotificationsForUser(userRole: String, classNumber: Int): Flow<List<NotificationRecordEntity>> =
        dao.getNotificationsForUser(userRole, classNumber)

    suspend fun insertNotification(notification: NotificationRecordEntity): Long =
        dao.insertNotification(notification)

    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)
    suspend fun deleteNotification(id: Long) = dao.deleteNotification(id)
    suspend fun clearAllNotifications() = dao.clearAllNotifications()

    // Device Tokens
    suspend fun getDeviceTokenForUser(userId: String): DeviceTokenEntity? =
        dao.getDeviceTokenForUser(userId)

    suspend fun saveDeviceToken(token: DeviceTokenEntity): Long =
        dao.insertDeviceToken(token)

    suspend fun deleteDeviceToken(userId: String) =
        dao.deleteDeviceToken(userId)

    // Preferences
    fun getPreferenceForUser(userId: String): Flow<NotificationPreferenceEntity?> =
        dao.getPreferenceForUser(userId)

    suspend fun savePreference(pref: NotificationPreferenceEntity): Long =
        dao.insertOrUpdatePreference(pref)

    // Quizzes & Competitions
    fun getQuizzesByClass(classNumber: Int): Flow<List<QuizEntity>> =
        dao.getQuizzesByClass(classNumber)

    suspend fun insertQuiz(quiz: QuizEntity): Long = dao.insertQuiz(quiz)
    suspend fun updateQuiz(quiz: QuizEntity) = dao.updateQuiz(quiz)

    suspend fun insertCompetition(comp: CompetitionEntity): Long = dao.insertCompetition(comp)
    suspend fun updateCompetition(comp: CompetitionEntity) = dao.updateCompetition(comp)

    // Complex Domain Calculations
    fun calculateGrade(percentage: Double): String = when {
        percentage >= 90.0 -> "A+"
        percentage >= 80.0 -> "A"
        percentage >= 70.0 -> "B+"
        percentage >= 60.0 -> "B"
        percentage >= 50.0 -> "C"
        percentage >= 40.0 -> "D"
        else -> "F (Fail)"
    }

    suspend fun calculateStudentResult(examId: Long, studentId: Long): StudentExamResult? {
        val student = dao.getStudentById(studentId) ?: return null
        val exam = dao.getExamById(examId) ?: return null
        val marks = dao.getMarksForStudentExam(examId, studentId).firstOrNull() ?: emptyList()
        val allExamMarks = dao.getAllMarksForExam(examId).firstOrNull() ?: emptyList()

        val totalObtained = marks.sumOf { it.marksObtained }
        val maxMarks = marks.sumOf { it.maxMarks }.takeIf { it > 0 } ?: 1
        val percentage = (totalObtained.toDouble() / maxMarks.toDouble()) * 100.0
        val isPassed = marks.all { (it.marksObtained.toDouble() / it.maxMarks.toDouble()) >= 0.33 } && percentage >= 35.0

        // Class rank calculation
        val allStudentsList = dao.getAllStudents().firstOrNull() ?: emptyList()
        val classStudents = allStudentsList.filter { it.classNumber == student.classNumber }

        val studentTotalsInClass = classStudents.map { s ->
            val sMarks = allExamMarks.filter { it.studentId == s.id }
            val sTotal = sMarks.sumOf { it.marksObtained }
            s.id to sTotal
        }.sortedByDescending { it.second }

        val classRank = (studentTotalsInClass.indexOfFirst { it.first == student.id } + 1).coerceAtLeast(1)

        val studentTotalsInSchool = allStudentsList.map { s ->
            val sMarks = allExamMarks.filter { it.studentId == s.id }
            val sTotal = sMarks.sumOf { it.marksObtained }
            s.id to sTotal
        }.sortedByDescending { it.second }

        val schoolRank = (studentTotalsInSchool.indexOfFirst { it.first == student.id } + 1).coerceAtLeast(1)

        val teacherRemark = marks.firstOrNull()?.teacherRemarks.takeUnless { it.isNullOrBlank() }
            ?: if (isPassed) "Satisfactory academic performance" else "Requires improvement and remedial sessions"

        return StudentExamResult(
            student = student,
            exam = exam,
            subjectMarks = marks,
            totalObtained = totalObtained,
            maxMarks = maxMarks,
            percentage = percentage,
            grade = calculateGrade(percentage),
            isPassed = isPassed,
            classRank = classRank,
            schoolRank = schoolRank,
            remarks = teacherRemark
        )
    }

    suspend fun computeRankings(examId: Long): List<TopperInfo> {
        val allStudentsList = dao.getAllStudents().firstOrNull() ?: emptyList()
        val allExamMarks = dao.getAllMarksForExam(examId).firstOrNull() ?: emptyList()

        return allStudentsList.mapNotNull { student ->
            val sMarks = allExamMarks.filter { it.studentId == student.id }
            if (sMarks.isEmpty()) return@mapNotNull null
            val totalObtained = sMarks.sumOf { it.marksObtained }
            val maxMarks = sMarks.sumOf { it.maxMarks }
            val pct = if (maxMarks > 0) (totalObtained.toDouble() / maxMarks) * 100.0 else 0.0
            TopperInfo(
                student = student,
                totalObtained = totalObtained,
                maxMarks = maxMarks,
                percentage = pct,
                rank = 0,
                grade = calculateGrade(pct)
            )
        }.sortedByDescending { it.percentage }
        .mapIndexed { idx, item -> item.copy(rank = idx + 1) }
    }

    suspend fun computeSubjectToppers(examId: Long): List<SubjectTopperInfo> {
        val allStudentsList = dao.getAllStudents().firstOrNull() ?: emptyList()
        val allExamMarks = dao.getAllMarksForExam(examId).firstOrNull() ?: emptyList()
        val studentMap = allStudentsList.associateBy { it.id }

        val subjects = allExamMarks.map { it.subject }.distinct()
        return subjects.mapNotNull { subj ->
            val highestMark = allExamMarks.filter { it.subject == subj }.maxByOrNull { it.marksObtained }
            val student = highestMark?.let { studentMap[it.studentId] }
            if (highestMark != null && student != null) {
                SubjectTopperInfo(
                    subject = subj,
                    student = student,
                    marksObtained = highestMark.marksObtained,
                    maxMarks = highestMark.maxMarks
                )
            } else null
        }
    }

    fun computeAttendanceSummary(attendanceList: List<AttendanceEntity>): AttendanceSummary {
        val totalDays = attendanceList.size
        val presentDays = attendanceList.count { it.status.equals("Present", ignoreCase = true) }
        val absentDays = attendanceList.count { it.status.equals("Absent", ignoreCase = true) }
        val leaveDays = attendanceList.count { it.status.equals("Leave", ignoreCase = true) }
        val pct = if (totalDays > 0) (presentDays.toDouble() / totalDays.toDouble()) * 100.0 else 100.0

        return AttendanceSummary(
            totalDays = totalDays,
            presentDays = presentDays,
            absentDays = absentDays,
            leaveDays = leaveDays,
            percentage = pct
        )
    }

    // Video Lectures
    fun getVideoLecturesByClass(classNum: Int): Flow<List<VideoLectureEntity>> =
        dao.getVideoLecturesByClass(classNum)

    fun getVideoLecturesByClassAndSubject(classNum: Int, subject: String): Flow<List<VideoLectureEntity>> =
        dao.getVideoLecturesByClassAndSubject(classNum, subject)

    suspend fun insertVideoLecture(lecture: VideoLectureEntity): Long =
        dao.insertVideoLecture(lecture)

    suspend fun deleteVideoLecture(id: Long) = dao.deleteVideoLecture(id)

    // Peer & Teacher Chat
    fun getChatBetween(userA: String, userB: String): Flow<List<PeerChatMessageEntity>> =
        dao.getChatBetween(userA, userB)

    fun getClassGroupChat(classNum: Int, section: String): Flow<List<PeerChatMessageEntity>> =
        dao.getClassGroupChat(classNum, section)

    suspend fun sendChatMessage(msg: PeerChatMessageEntity): Long =
        dao.insertChatMessage(msg)

    // Student Applications to Sir
    fun getApplicationsByStudent(studentId: String): Flow<List<StudentApplicationEntity>> =
        dao.getApplicationsByStudent(studentId)

    fun getApplicationsByClass(classNum: Int): Flow<List<StudentApplicationEntity>> =
        dao.getApplicationsByClass(classNum)

    suspend fun submitApplication(app: StudentApplicationEntity): Long =
        dao.insertApplication(app)

    suspend fun reviewApplication(id: Long, status: String, remarks: String, reviewer: String, date: String) =
        dao.updateApplicationStatus(id, status, remarks, reviewer, date)

    // User Accounts
    suspend fun loginUser(mobile: String, pass: String): UserAccountEntity? =
        dao.getUserAccount(mobile, pass)

    suspend fun loginStudentByRoll(classNum: Int, section: String, roll: Int, pass: String): UserAccountEntity? {
        val account = dao.getUserAccountByRoll(classNum, section, roll)
        return if (account != null && account.password == pass) account else null
    }

    suspend fun registerUser(account: UserAccountEntity): Long =
        dao.insertUserAccount(account)
}
