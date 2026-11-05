package com.example

import com.example.ai.AiLanguage
import com.example.ai.SchoolAiTeacherService
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testGradeCalculations() {
        // Test grade calculation ranges
        assertEquals("A+", calculateMockGrade(95.0))
        assertEquals("A", calculateMockGrade(84.0))
        assertEquals("B+", calculateMockGrade(72.0))
        assertEquals("B", calculateMockGrade(65.0))
        assertEquals("C", calculateMockGrade(52.0))
        assertEquals("D", calculateMockGrade(41.0))
        assertEquals("F (Fail)", calculateMockGrade(32.0))
    }

    private fun calculateMockGrade(percentage: Double): String = when {
        percentage >= 90.0 -> "A+"
        percentage >= 80.0 -> "A"
        percentage >= 70.0 -> "B+"
        percentage >= 60.0 -> "B"
        percentage >= 50.0 -> "C"
        percentage >= 40.0 -> "D"
        else -> "F (Fail)"
    }

    @Test
    fun testAiTeacherServiceMathResponse() = runBlocking {
        val answer = SchoolAiTeacherService.getAiAnswer(
            query = "How to solve quadratic equations?",
            classLevel = 10,
            language = AiLanguage.ENGLISH
        )
        assertNotNull(answer)
        assertTrue(answer.contains("M.I.C.D AI TEACHER"))
    }

    @Test
    fun testAiTeacherServiceHindiResponse() = runBlocking {
        val answer = SchoolAiTeacherService.getAiAnswer(
            query = "न्यूटन के नियम समझाइए",
            classLevel = 9,
            language = AiLanguage.HINDI
        )
        assertNotNull(answer)
        assertTrue(answer.contains("एम.आई.सी.डी एआई शिक्षक"))
    }

    @Test
    fun testMandatoryNotificationChannels() {
        assertEquals("school_announcements", com.example.notification.MedianNotificationManager.CHANNEL_SCHOOL_ANNOUNCEMENTS)
        assertEquals("homework_updates", com.example.notification.MedianNotificationManager.CHANNEL_HOMEWORK_UPDATES)
        assertEquals("examination_notices", com.example.notification.MedianNotificationManager.CHANNEL_EXAMINATION_NOTICES)
        assertEquals("quiz_notifications", com.example.notification.MedianNotificationManager.CHANNEL_QUIZ_NOTIFICATIONS)
        assertEquals("competition_notifications", com.example.notification.MedianNotificationManager.CHANNEL_COMPETITION_NOTIFICATIONS)
    }

    @Test
    fun testFcmTopicFormatters() {
        assertEquals("class_10", com.example.notification.MedianNotificationManager.getTopicForClass(10))
        assertEquals("class_1", com.example.notification.MedianNotificationManager.getTopicForClass(1))
        assertEquals("class_12", com.example.notification.MedianNotificationManager.getTopicForClass(12))
        assertEquals("all_students", com.example.notification.MedianNotificationManager.TOPIC_ALL_STUDENTS)
        assertEquals("all_teachers", com.example.notification.MedianNotificationManager.TOPIC_ALL_TEACHERS)
    }
}
