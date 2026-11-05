package com.example.ui.state

enum class AppLanguage(val label: String, val code: String) {
    ENGLISH("English", "en"),
    HINDI("हिन्दी", "hi")
}

enum class UserRole(val englishName: String, val hindiName: String) {
    ADMIN("Admin", "प्रशासक"),
    TEACHER("Teacher", "शिक्षक"),
    STUDENT("Student", "छात्र"),
    PARENT("Parent", "अभिभावक")
}

object AppStrings {
    fun appTitle(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "एम.आई.सी.डी. स्कूल" else "M.I.C.D. School"
    fun appTagline(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "एआई-संचालित स्मार्ट स्कूल प्रबंधन" else "AI-Powered School Management System"

    fun roleLabel(role: UserRole, lang: AppLanguage) = if (lang == AppLanguage.HINDI) role.hindiName else role.englishName

    fun adminPanel(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "प्रशासन पैनल" else "Admin Panel"
    fun teacherPanel(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "शिक्षक पैनल" else "Teacher Panel"
    fun studentPanel(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "छात्र पैनल" else "Student Panel"
    fun parentPanel(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "अभिभावक पैनल" else "Parent Panel"

    fun dashboard(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "डैशबोर्ड" else "Dashboard"
    fun students(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "छात्र" else "Students"
    fun teachers(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "शिक्षक" else "Teachers"
    fun attendance(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "उपस्थिति" else "Attendance"
    fun homework(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "गृहकार्य" else "Homework"
    fun studyNotes(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "अध्ययन नोट्स" else "Study Notes"
    fun examinations(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "परीक्षाएं" else "Examinations"
    fun results(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "परीक्षा परिणाम" else "Results & Report Cards"
    fun toppers(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "टॉपर्स एवं रैंकिंग" else "Toppers & Rankings"
    fun timetable(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "समय-सारणी" else "Timetable"
    fun notices(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "सूचना पट्ट" else "Notice Board"
    fun aiTeacher(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "एम.आई.सी.डी एआई शिक्षक" else "M.I.C.D AI Teacher"

    fun filterAll(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "सभी" else "All"
    fun filterBoys(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "लड़के (Boys)" else "Boys"
    fun filterGirls(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "लड़कियां (Girls)" else "Girls"

    fun statusPresent(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "उपस्थित" else "Present"
    fun statusAbsent(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "अनुपस्थित" else "Absent"
    fun statusLeave(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "अवकाश" else "Leave"

    fun rollNumber(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "क्रमांक (Roll No)" else "Roll No"
    fun admissionNumber(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "प्रवेश संख्या" else "Admission No"
    fun fatherName(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "पिता का नाम" else "Father's Name"
    fun motherName(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "माता का नाम" else "Mother's Name"
    fun dateOfBirth(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "जन्म तिथि" else "Date of Birth"
    fun classAndSection(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "कक्षा एवं वर्ग" else "Class & Section"

    fun markAttendance(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "उपस्थिति दर्ज करें" else "Mark Attendance"
    fun uploadHomework(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "गृहकार्य अपलोड करें" else "Upload Homework"
    fun uploadNotes(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "नोट्स अपलोड करें" else "Upload Notes"
    fun enterMarks(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "अंक प्रविष्ट करें" else "Enter Marks"
    fun publishResults(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "परिणाम प्रकाशित करें" else "Publish Results"

    fun switchRole(lang: AppLanguage) = if (lang == AppLanguage.HINDI) "भूमिका बदलें" else "Switch Panel"
}
