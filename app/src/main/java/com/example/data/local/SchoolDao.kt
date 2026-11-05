package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    // ---------------- Students ----------------
    @Query("SELECT * FROM students ORDER BY classNumber ASC, section ASC, rollNumber ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE classNumber = :classNumber AND section = :section ORDER BY rollNumber ASC")
    fun getStudentsByClassAndSection(classNumber: Int, section: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE classNumber = :classNumber ORDER BY rollNumber ASC")
    fun getStudentsByClass(classNumber: Int): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE admissionNo = :admissionNo LIMIT 1")
    suspend fun getStudentByAdmissionNo(admissionNo: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    // ---------------- Teachers ----------------
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
    suspend fun getTeacherById(id: Long): TeacherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun deleteTeacherById(id: Long)

    // ---------------- Attendance ----------------
    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE classNumber = :classNumber AND section = :section AND date = :date")
    fun getAttendanceByClassDate(classNumber: Int, section: String, date: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: List<AttendanceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE classNumber = :classNumber AND section = :section AND date = :date")
    suspend fun clearAttendanceForDate(classNumber: Int, section: String, date: String)

    // ---------------- Homework ----------------
    @Query("SELECT * FROM homework WHERE classNumber = :classNumber AND (section = :section OR section = 'All') ORDER BY dueDate DESC")
    fun getHomeworkForClass(classNumber: Int, section: String): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework ORDER BY dueDate DESC")
    fun getAllHomework(): Flow<List<HomeworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworkList(list: List<HomeworkEntity>)

    @Query("DELETE FROM homework WHERE id = :id")
    suspend fun deleteHomework(id: Long)

    // ---------------- Study Notes ----------------
    @Query("SELECT * FROM study_notes WHERE isApproved = 1 ORDER BY classNumber ASC, chapterNumber ASC")
    fun getAllApprovedNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE classNumber = :classNumber AND isApproved = 1 ORDER BY chapterNumber ASC")
    fun getNotesByClass(classNumber: Int): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes ORDER BY dateAdded DESC")
    fun getAllNotesAdmin(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE isBookmarked = 1")
    fun getBookmarkedNotes(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<StudyNoteEntity>)

    @Update
    suspend fun updateNote(note: StudyNoteEntity)

    @Query("DELETE FROM study_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    // ---------------- Exams & Marks ----------------
    @Query("SELECT * FROM exams ORDER BY id DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :examId LIMIT 1")
    suspend fun getExamById(examId: Long): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Query("SELECT * FROM exam_marks WHERE examId = :examId AND studentId = :studentId")
    fun getMarksForStudentExam(examId: Long, studentId: Long): Flow<List<ExamMarkEntity>>

    @Query("SELECT * FROM exam_marks WHERE examId = :examId")
    fun getAllMarksForExam(examId: Long): Flow<List<ExamMarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamMark(mark: ExamMarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamMarks(marks: List<ExamMarkEntity>)

    // ---------------- Notices ----------------
    @Query("SELECT * FROM notices ORDER BY isUrgent DESC, id DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: Long)

    // ---------------- Timetable ----------------
    @Query("SELECT * FROM timetables WHERE classNumber = :classNumber AND section = :section ORDER BY periodNumber ASC")
    fun getTimetableForClass(classNumber: Int, section: String): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetables WHERE teacherName = :teacherName ORDER BY dayOfWeek ASC, periodNumber ASC")
    fun getTimetableForTeacher(teacherName: String): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableEntries(entries: List<TimetableEntity>)

    // ---------------- AI Chat ----------------
    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp ASC")
    fun getAllAiMessages(): Flow<List<AiChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessage(message: AiChatMessageEntity): Long

    @Query("DELETE FROM ai_chat_messages")
    suspend fun clearAiMessages()

    // ---------------- Push Notifications ----------------
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationRecordEntity>>

    @Query("SELECT * FROM notifications WHERE (targetRole = 'ALL' OR targetRole = :userRole) AND (targetClass = 0 OR targetClass = :classNumber) ORDER BY timestamp DESC")
    fun getNotificationsForUser(userRole: String, classNumber: Int): Flow<List<NotificationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationRecordEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // ---------------- Device Tokens ----------------
    @Query("SELECT * FROM device_tokens WHERE userId = :userId LIMIT 1")
    suspend fun getDeviceTokenForUser(userId: String): DeviceTokenEntity?

    @Query("SELECT * FROM device_tokens ORDER BY lastUpdated DESC")
    fun getAllDeviceTokens(): Flow<List<DeviceTokenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceToken(token: DeviceTokenEntity): Long

    @Query("DELETE FROM device_tokens WHERE userId = :userId")
    suspend fun deleteDeviceToken(userId: String)

    // ---------------- Notification Preferences ----------------
    @Query("SELECT * FROM notification_preferences WHERE userId = :userId LIMIT 1")
    fun getPreferenceForUser(userId: String): Flow<NotificationPreferenceEntity?>

    @Query("SELECT * FROM notification_preferences WHERE userId = :userId LIMIT 1")
    suspend fun getPreferenceDirect(userId: String): NotificationPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreference(preference: NotificationPreferenceEntity): Long

    // ---------------- Quizzes ----------------
    @Query("SELECT * FROM quizzes ORDER BY id DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE classNumber = :classNumber ORDER BY id DESC")
    fun getQuizzesByClass(classNumber: Int): Flow<List<QuizEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    // ---------------- Competitions ----------------
    @Query("SELECT * FROM competitions ORDER BY id DESC")
    fun getAllCompetitions(): Flow<List<CompetitionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetition(competition: CompetitionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetitions(competitions: List<CompetitionEntity>)

    @Update
    suspend fun updateCompetition(competition: CompetitionEntity)

    // ---------------- Peer & Teacher Chat ----------------
    @Query("SELECT * FROM peer_chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<PeerChatMessageEntity>>

    @Query("SELECT * FROM peer_chat_messages WHERE (senderId = :userA AND recipientId = :userB) OR (senderId = :userB AND recipientId = :userA) ORDER BY timestamp ASC")
    fun getChatBetween(userA: String, userB: String): Flow<List<PeerChatMessageEntity>>

    @Query("SELECT * FROM peer_chat_messages WHERE classNumber = :classNumber AND section = :section AND chatType = 'CLASS_GROUP' ORDER BY timestamp ASC")
    fun getClassGroupChat(classNumber: Int, section: String): Flow<List<PeerChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: PeerChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<PeerChatMessageEntity>)

    // ---------------- Student Applications (Leave/Permission to Sir) ----------------
    @Query("SELECT * FROM student_applications ORDER BY id DESC")
    fun getAllApplications(): Flow<List<StudentApplicationEntity>>

    @Query("SELECT * FROM student_applications WHERE studentId = :studentId ORDER BY id DESC")
    fun getApplicationsByStudent(studentId: String): Flow<List<StudentApplicationEntity>>

    @Query("SELECT * FROM student_applications WHERE classNumber = :classNumber ORDER BY id DESC")
    fun getApplicationsByClass(classNumber: Int): Flow<List<StudentApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(app: StudentApplicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(apps: List<StudentApplicationEntity>)

    @Update
    suspend fun updateApplication(app: StudentApplicationEntity)

    @Query("UPDATE student_applications SET status = :status, teacherRemarks = :remarks, reviewedBy = :reviewedBy, reviewedDate = :date WHERE id = :id")
    suspend fun updateApplicationStatus(id: Long, status: String, remarks: String, reviewedBy: String, date: String)

    // ---------------- Video Lectures (Recorded Lectures) ----------------
    @Query("SELECT * FROM video_lectures ORDER BY id DESC")
    fun getAllVideoLectures(): Flow<List<VideoLectureEntity>>

    @Query("SELECT * FROM video_lectures WHERE classNumber = :classNumber ORDER BY id DESC")
    fun getVideoLecturesByClass(classNumber: Int): Flow<List<VideoLectureEntity>>

    @Query("SELECT * FROM video_lectures WHERE classNumber = :classNumber AND subject = :subject ORDER BY id DESC")
    fun getVideoLecturesByClassAndSubject(classNumber: Int, subject: String): Flow<List<VideoLectureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoLecture(lecture: VideoLectureEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoLectures(lectures: List<VideoLectureEntity>)

    @Query("DELETE FROM video_lectures WHERE id = :id")
    suspend fun deleteVideoLecture(id: Long)

    // ---------------- User Accounts (Login & Registration) ----------------
    @Query("SELECT * FROM user_accounts ORDER BY id DESC")
    fun getAllUserAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE mobileNumber = :mobile AND password = :pass LIMIT 1")
    suspend fun getUserAccount(mobile: String, pass: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE classNumber = :classNumber AND section = :section AND rollNumber = :rollNumber LIMIT 1")
    suspend fun getUserAccountByRoll(classNumber: Int, section: String, rollNumber: Int): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE mobileNumber = :mobile LIMIT 1")
    suspend fun getUserAccountByMobile(mobile: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: UserAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccounts(accounts: List<UserAccountEntity>)

    @Update
    suspend fun updateUserAccount(account: UserAccountEntity)
}
