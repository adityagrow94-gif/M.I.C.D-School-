package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        StudentEntity::class,
        TeacherEntity::class,
        AttendanceEntity::class,
        HomeworkEntity::class,
        StudyNoteEntity::class,
        ExamEntity::class,
        ExamMarkEntity::class,
        NoticeEntity::class,
        TimetableEntity::class,
        AiChatMessageEntity::class,
        NotificationRecordEntity::class,
        DeviceTokenEntity::class,
        NotificationPreferenceEntity::class,
        QuizEntity::class,
        CompetitionEntity::class,
        PeerChatMessageEntity::class,
        StudentApplicationEntity::class,
        VideoLectureEntity::class,
        UserAccountEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "micd_school_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
