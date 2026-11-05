package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,          // e.g., "MICD-STU-001"
    val admissionNo: String,        // e.g., "ADM-2024-101"
    val name: String,
    val fatherName: String,
    val motherName: String,
    val dob: String,               // YYYY-MM-DD
    val gender: String,            // "Boy" or "Girl"
    val classNumber: Int,          // 1 to 12
    val section: String,           // "A", "B", "C"
    val rollNumber: Int,           // 1 to 100
    val contactNumber: String,
    val email: String = "",
    val address: String = "Campus Road, City",
    val photoUrl: String = "",
    val academicYear: String = "2024-2025"
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: String,          // e.g., "MICD-TCH-01"
    val name: String,
    val email: String,
    val phone: String,
    val qualification: String,
    val assignedClasses: String,    // Comma-separated: "9,10"
    val assignedSubjects: String,   // Comma-separated: "Mathematics,Science"
    val isClassTeacherOf: String = "" // "10-A"
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String,               // YYYY-MM-DD
    val status: String,             // "Present", "Absent", "Leave"
    val classNumber: Int,
    val section: String,
    val markedByTeacherId: String = ""
)

@Entity(tableName = "homework")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val classNumber: Int,
    val section: String,
    val subject: String,
    val teacherName: String,
    val assignedDate: String,
    val dueDate: String,
    val attachmentType: String = "Text", // "PDF", "Image", "Text"
    val attachmentContent: String = ""
)

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val classNumber: Int,
    val subject: String,
    val chapterNumber: Int,
    val chapterName: String,
    val topic: String,
    val content: String,
    val authorName: String,
    val isApproved: Boolean = true,
    val dateAdded: String,
    val isBookmarked: Boolean = false,
    val formatType: String = "Notes" // "Notes", "PDF Summary", "Worksheet", "MCQ"
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examName: String,           // e.g. "Annual Examination 2024"
    val examType: String,           // "Annual", "Half-Yearly", "Monthly", "Weekly", "Unit Test", "Pre-Board", "Practical"
    val academicYear: String = "2024-2025",
    val isPublished: Boolean = true,
    val startDate: String,
    val endDate: String
)

@Entity(tableName = "exam_marks")
data class ExamMarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val studentId: Long,
    val subject: String,
    val maxMarks: Int = 100,
    val marksObtained: Int,
    val teacherRemarks: String = "Good performance"
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String,           // "General", "Holiday", "Exam", "Parent Meeting", "Result"
    val date: String,
    val author: String = "Principal Office",
    val isUrgent: Boolean = false
)

@Entity(tableName = "timetables")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classNumber: Int,
    val section: String,
    val dayOfWeek: String,          // "Monday", "Tuesday", etc.
    val periodNumber: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val teacherName: String
)

@Entity(tableName = "ai_chat_messages")
data class AiChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,               // "user", "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "English",
    val classLevel: Int = 10
)

@Entity(tableName = "notifications")
data class NotificationRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val notificationId: String,     // e.g. "NOTIF-2024-001"
    val title: String,
    val message: String,
    val channelId: String,          // "school_announcements", "homework_updates", "examination_notices", "quiz_notifications", "competition_notifications"
    val targetRole: String = "ALL", // "ALL", "STUDENTS", "TEACHERS", "PARENTS"
    val targetClass: Int = 0,       // 0 for all classes, 1-12
    val targetSection: String = "ALL",
    val targetUrl: String = "",     // e.g., "micdschool://results", "micdschool://homework"
    val senderRole: String = "SUPER_ADMIN", // "SUPER_ADMIN", "PRINCIPAL_ADMIN", "TEACHER"
    val senderName: String = "Admin Office",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED", // "DELIVERED", "SENT", "PENDING", "FAILED"
    val isRead: Boolean = false
)

@Entity(tableName = "device_tokens")
data class DeviceTokenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,             // Student ID, Teacher ID, or Parent ID
    val userRole: String,           // "STUDENT", "TEACHER", "PARENT", "ADMIN"
    val fcmToken: String,
    val devicePlatform: String = "Android - Median",
    val isMedianBridge: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "notification_preferences")
data class NotificationPreferenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,             // Student ID, Teacher ID, etc.
    val enableAll: Boolean = true,
    val announcements: Boolean = true,
    val homework: Boolean = true,
    val examination: Boolean = true,
    val quizAlerts: Boolean = true,
    val competitionAlerts: Boolean = true,
    val importantNotices: Boolean = true
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: String,
    val title: String,
    val subject: String,
    val classNumber: Int,
    val deadlineDate: String,
    val totalMarks: Int = 25,
    val status: String = "PUBLISHED" // "PUBLISHED", "UPCOMING", "COMPLETED"
)

@Entity(tableName = "competitions")
data class CompetitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val competitionId: String,
    val title: String,
    val category: String,           // "Science Fair", "Math Olympiad", "Coding Contest", "Debate"
    val eligibleClasses: String,    // "6,7,8,9,10,11,12"
    val registrationDeadline: String,
    val eventDate: String,
    val status: String = "REGISTRATION_OPEN", // "REGISTRATION_OPEN", "STARTING_SOON", "ONGOING", "RESULTS_PUBLISHED"
    val winnerStudentName: String = ""
)

@Entity(tableName = "peer_chat_messages")
data class PeerChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,           // e.g. "MICD-STU-001" or Teacher ID
    val senderName: String,
    val senderRole: String,         // "STUDENT", "TEACHER", "ADMIN"
    val recipientId: String,       // e.g. "MICD-STU-002" or "ALL_CLASS_10A" or "TEACHER_MATH"
    val recipientName: String,
    val message: String,
    val classNumber: Int,
    val section: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val chatType: String = "STUDENT_TO_STUDENT" // "STUDENT_TO_STUDENT", "STUDENT_TO_TEACHER", "CLASS_GROUP"
)

@Entity(tableName = "student_applications")
data class StudentApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val applicationId: String,      // e.g. "APP-2024-001"
    val studentId: String,
    val studentName: String,
    val classNumber: Int,
    val section: String,
    val rollNumber: Int,
    val applicationType: String,    // "LEAVE", "FEE_CONCESSION", "BONAFIDE", "COMPLAINT", "GENERAL"
    val subject: String,
    val reason: String,
    val startDate: String = "",
    val endDate: String = "",
    val submittedDate: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val teacherRemarks: String = "",
    val reviewedBy: String = "",    // "Vikramaditya Sharma Sir" / "Principal Sir"
    val reviewedDate: String = ""
)

@Entity(tableName = "video_lectures")
data class VideoLectureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val classNumber: Int,
    val chapterNumber: Int,
    val chapterName: String,
    val teacherName: String,
    val durationMinutes: Int,
    val videoUrl: String,           // Direct MP4 / streaming URL or web link
    val thumbnailUrl: String = "",
    val description: String,
    val uploadDate: String,
    val viewCount: Int = 124,
    val hasNotes: Boolean = true
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String,
    val rollNumber: Int,
    val classNumber: Int,
    val section: String,            // "A", "B", "C" (varg)
    val mobileNumber: String,
    val parentsName: String,        // Father / Mother name
    val password: String,
    val role: String = "STUDENT",   // "STUDENT", "TEACHER", "ADMIN", "PARENT"
    val isLoggedIn: Boolean = false,
    val registeredDate: Long = System.currentTimeMillis()
)

