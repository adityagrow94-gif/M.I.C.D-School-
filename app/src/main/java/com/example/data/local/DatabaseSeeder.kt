package com.example.data.local

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    suspend fun seedDatabaseIfEmpty(dao: SchoolDao) = withContext(Dispatchers.IO) {
        // Only seed if empty
        val sampleStudent = dao.getStudentById(1)
        if (sampleStudent != null) return@withContext

        // 1. Seed Teachers
        val teachers = listOf(
            TeacherEntity(
                id = 1,
                teacherId = "TCH-001",
                name = "Mr. Rajesh Sharma",
                email = "rajesh.sharma@micdschool.edu",
                phone = "+91 98765 43210",
                qualification = "M.Sc. Mathematics, B.Ed.",
                assignedClasses = "9,10,11,12",
                assignedSubjects = "Mathematics",
                isClassTeacherOf = "10-A"
            ),
            TeacherEntity(
                id = 2,
                teacherId = "TCH-002",
                name = "Mrs. Sunita Verma",
                email = "sunita.verma@micdschool.edu",
                phone = "+91 98765 43211",
                qualification = "M.Sc. Physics, M.Ed.",
                assignedClasses = "8,9,10,11,12",
                assignedSubjects = "Science,Physics",
                isClassTeacherOf = "9-B"
            ),
            TeacherEntity(
                id = 3,
                teacherId = "TCH-003",
                name = "Dr. Anand Joshi",
                email = "anand.joshi@micdschool.edu",
                phone = "+91 98765 43212",
                qualification = "Ph.D. Chemistry",
                assignedClasses = "10,11,12",
                assignedSubjects = "Chemistry,Science",
                isClassTeacherOf = "12-A"
            ),
            TeacherEntity(
                id = 4,
                teacherId = "TCH-004",
                name = "Mrs. Priya Nair",
                email = "priya.nair@micdschool.edu",
                phone = "+91 98765 43213",
                qualification = "M.A. English Literature",
                assignedClasses = "6,7,8,9,10",
                assignedSubjects = "English",
                isClassTeacherOf = "8-A"
            ),
            TeacherEntity(
                id = 5,
                teacherId = "TCH-005",
                name = "Mr. Vikramaditya Singh",
                email = "vikram.singh@micdschool.edu",
                phone = "+91 98765 43214",
                qualification = "M.A. Hindi, B.Ed.",
                assignedClasses = "1,2,3,4,5,6,7,8,9,10",
                assignedSubjects = "Hindi",
                isClassTeacherOf = "7-B"
            ),
            TeacherEntity(
                id = 6,
                teacherId = "TCH-006",
                name = "Mrs. Neha Gupta",
                email = "neha.gupta@micdschool.edu",
                phone = "+91 98765 43215",
                qualification = "MCA, B.Tech Computer Science",
                assignedClasses = "6,7,8,9,10,11,12",
                assignedSubjects = "Computer Science",
                isClassTeacherOf = "11-A"
            )
        )
        dao.insertTeachers(teachers)

        // 2. Seed Students across Classes 1 to 12
        val students = mutableListOf<StudentEntity>()
        val boyFirstNames = listOf("Aarav", "Kabir", "Vihaan", "Aditya", "Rohan", "Vivaan", "Reyansh", "Arjun", "Sai", "Dhruv", "Aryan", "Atharva")
        val girlFirstNames = listOf("Ananya", "Diya", "Isha", "Saanvi", "Myra", "Aadhya", "Pari", "Navya", "Riya", "Avani", "Kavya", "Sneha")
        val lastNames = listOf("Sharma", "Verma", "Gupta", "Patel", "Singh", "Kumar", "Mishra", "Joshi", "Yadav", "Chauhan", "Mehta", "Bose")
        val fatherFirstNames = listOf("Ramesh", "Suresh", "Mahesh", "Dinesh", "Rajesh", "Pankaj", "Vikas", "Sanjay", "Alok", "Sunil")
        val motherFirstNames = listOf("Kavita", "Sita", "Geeta", "Sunita", "Pooja", "Rekha", "Meena", "Shashi", "Anju", "Sarita")

        var globalId = 1L
        // Seed students for Classes 1 to 12
        for (c in 1..12) {
            val sections = listOf("A", "B", "C")
            for (sec in sections) {
                // Seed 8-10 students per section with Boys and Girls, roll numbers 1, 2, 3...
                val countPerSection = 8
                for (roll in 1..countPerSection) {
                    val isGirl = roll % 2 == 0
                    val fName = if (isGirl) girlFirstNames[(roll + c) % girlFirstNames.size] else boyFirstNames[(roll + c) % boyFirstNames.size]
                    val lName = lastNames[(roll * 3 + c) % lastNames.size]
                    val father = "${fatherFirstNames[(roll + c) % fatherFirstNames.size]} $lName"
                    val mother = "${motherFirstNames[(roll + c) % motherFirstNames.size]} $lName"
                    val gender = if (isGirl) "Girl" else "Boy"
                    val dobYear = 2024 - (5 + c)
                    val dobMonth = (roll % 12 + 1).toString().padStart(2, '0')
                    val dobDay = (roll % 28 + 1).toString().padStart(2, '0')

                    students.add(
                        StudentEntity(
                            id = globalId,
                            studentId = "MICD-${c}${sec}-${roll.toString().padStart(3, '0')}",
                            admissionNo = "ADM-${2020 + (c % 4)}-${globalId.toString().padStart(4, '0')}",
                            name = "$fName $lName",
                            fatherName = father,
                            motherName = mother,
                            dob = "$dobYear-$dobMonth-$dobDay",
                            gender = gender,
                            classNumber = c,
                            section = sec,
                            rollNumber = roll,
                            contactNumber = "+91 98${(10000000 + globalId * 12345).toString().take(8)}",
                            email = "${fName.lowercase()}.${lName.lowercase()}@micdstudent.edu",
                            address = "Sector ${(roll * 2) % 25 + 1}, Garden Enclave, City",
                            photoUrl = "",
                            academicYear = "2024-2025"
                        )
                    )
                    globalId++
                }
            }
        }
        dao.insertStudents(students)

        // 3. Seed Attendance Records for students in recent days
        val dates = listOf("2024-11-01", "2024-11-02", "2024-11-03", "2024-11-04")
        val attendanceList = mutableListOf<AttendanceEntity>()
        for (student in students.take(60)) {
            for (date in dates) {
                val status = when ((student.rollNumber + date.hashCode()) % 15) {
                    0 -> "Absent"
                    1 -> "Leave"
                    else -> "Present"
                }
                attendanceList.add(
                    AttendanceEntity(
                        studentId = student.id,
                        date = date,
                        status = status,
                        classNumber = student.classNumber,
                        section = student.section,
                        markedByTeacherId = "TCH-001"
                    )
                )
            }
        }
        dao.insertAttendance(attendanceList)

        // 4. Seed Exams
        val exams = listOf(
            ExamEntity(
                id = 1,
                examName = "Half-Yearly Examination 2024",
                examType = "Half-Yearly",
                academicYear = "2024-2025",
                isPublished = true,
                startDate = "2024-09-15",
                endDate = "2024-09-25"
            ),
            ExamEntity(
                id = 2,
                examName = "Monthly Unit Test - October",
                examType = "Monthly",
                academicYear = "2024-2025",
                isPublished = true,
                startDate = "2024-10-20",
                endDate = "2024-10-24"
            ),
            ExamEntity(
                id = 3,
                examName = "Annual Examination 2025",
                examType = "Annual",
                academicYear = "2024-2025",
                isPublished = false,
                startDate = "2025-03-01",
                endDate = "2025-03-15"
            )
        )
        dao.insertExams(exams)

        // 5. Seed Exam Marks for Half-Yearly (Exam 1)
        val examMarks = mutableListOf<ExamMarkEntity>()
        val coreSubjects = listOf("Mathematics", "Science", "English", "Hindi", "Social Science")
        for (student in students) {
            // Assign marks in a realistic distribution, giving higher marks to lower roll numbers for demonstration
            val baseBonus = ((10 - student.rollNumber).coerceAtLeast(0)) * 2
            val randomFactor = (student.id % 7).toInt()
            for (subj in coreSubjects) {
                val mark = (70 + baseBonus + randomFactor + (subj.length % 5)).coerceIn(40, 99)
                val remarks = when {
                    mark >= 90 -> "Outstanding academic performance"
                    mark >= 80 -> "Very good concept clarity"
                    mark >= 60 -> "Good effort, can achieve more"
                    else -> "Needs revision in key chapters"
                }
                examMarks.add(
                    ExamMarkEntity(
                        examId = 1,
                        studentId = student.id,
                        subject = subj,
                        maxMarks = 100,
                        marksObtained = mark,
                        teacherRemarks = remarks
                    )
                )
            }
        }
        dao.insertExamMarks(examMarks)

        // 6. Seed Homework
        val homeworkList = listOf(
            HomeworkEntity(
                title = "Quadratic Equations Exercise 4.2",
                description = "Solve questions 1 to 10 from NCERT textbook with step-by-step working.",
                classNumber = 10,
                section = "A",
                subject = "Mathematics",
                teacherName = "Mr. Rajesh Sharma",
                assignedDate = "2024-11-01",
                dueDate = "2024-11-06",
                attachmentType = "PDF",
                attachmentContent = "Homework_Math_Ex4.2.pdf"
            ),
            HomeworkEntity(
                title = "Ray Optics - Reflection & Refraction Diagrams",
                description = "Draw ray diagrams for concave mirror when object is at C and between F and P.",
                classNumber = 10,
                section = "A",
                subject = "Science",
                teacherName = "Mrs. Sunita Verma",
                assignedDate = "2024-11-02",
                dueDate = "2024-11-07",
                attachmentType = "Image",
                attachmentContent = "Ray_Diagram_Reference.jpg"
            ),
            HomeworkEntity(
                title = "Hindi Nibandh: 'Paryavaran Sanrakshan'",
                description = "Write a 250-word essay on environmental conservation with suitable quotes.",
                classNumber = 9,
                section = "A",
                subject = "Hindi",
                teacherName = "Mr. Vikramaditya Singh",
                assignedDate = "2024-11-03",
                dueDate = "2024-11-08",
                attachmentType = "Text",
                attachmentContent = "Guidelines: Focus on water harvesting and afforestation."
            ),
            HomeworkEntity(
                title = "English Grammar: Active and Passive Voice",
                description = "Complete worksheet questions 1 to 20 and write 5 real-life examples.",
                classNumber = 8,
                section = "A",
                subject = "English",
                teacherName = "Mrs. Priya Nair",
                assignedDate = "2024-11-02",
                dueDate = "2024-11-05",
                attachmentType = "Worksheet",
                attachmentContent = "Voice_Change_Worksheet.pdf"
            ),
            HomeworkEntity(
                title = "Python Loops & List Comprehensions",
                description = "Write python script to print prime numbers between 1 and 100.",
                classNumber = 11,
                section = "A",
                subject = "Computer Science",
                teacherName = "Mrs. Neha Gupta",
                assignedDate = "2024-11-01",
                dueDate = "2024-11-07",
                attachmentType = "Text",
                attachmentContent = "Submission via school portal code runner."
            )
        )
        dao.insertHomeworkList(homeworkList)

        // 7. Seed Study Notes
        val notes = listOf(
            StudyNoteEntity(
                title = "Trigonometric Ratios & Identities Formulas",
                classNumber = 10,
                subject = "Mathematics",
                chapterNumber = 8,
                chapterName = "Introduction to Trigonometry",
                topic = "Trigonometric Identities & Values Table (0° to 90°)",
                content = """
                    ## Chapter 8: Introduction to Trigonometry
                    
                    ### Key Trigonometric Ratios (Right-angled Triangle ABC at B):
                    - sin A = Perpendicular / Hypotenuse = BC / AC
                    - cos A = Base / Hypotenuse = AB / AC
                    - tan A = Perpendicular / Base = BC / AB = sin A / cos A
                    - cosec A = 1 / sin A
                    - sec A = 1 / cos A
                    - cot A = 1 / tan A
                    
                    ### Standard Values (Must Remember):
                    | Ratio | 0° | 30° | 45° | 60° | 90° |
                    |-------|----|-----|-----|-----|-----|
                    | sin   | 0  | 1/2 | 1/√2| √3/2| 1   |
                    | cos   | 1  | √3/2| 1/√2| 1/2 | 0   |
                    | tan   | 0  | 1/√3| 1   | √3  | Not Defined |
                    
                    ### Fundamental Pythagorean Identities:
                    1. sin²θ + cos²θ = 1
                    2. 1 + tan²θ = sec²θ
                    3. 1 + cot²θ = cosec²θ
                """.trimIndent(),
                authorName = "Mr. Rajesh Sharma",
                isApproved = true,
                dateAdded = "2024-10-15",
                isBookmarked = true,
                formatType = "Notes"
            ),
            StudyNoteEntity(
                title = "Chemical Reactions & Equations - Full Summary",
                classNumber = 10,
                subject = "Science",
                chapterNumber = 1,
                chapterName = "Chemical Reactions",
                topic = "Types of Reactions, Balancing, Corrosion & Rancidity",
                content = """
                    ## Chapter 1: Chemical Reactions and Equations
                    
                    ### Characteristics of Chemical Reaction:
                    - Change in state
                    - Change in colour
                    - Evolution of gas
                    - Change in temperature
                    
                    ### Types of Chemical Reactions:
                    1. **Combination Reaction**: Two or more reactants combine to form a single product.
                       CaO(s) + H₂O(l) → Ca(OH)₂(aq) + Heat
                    2. **Decomposition Reaction**: Single reactant breaks down to give simpler products.
                       2FeSO₄(s) → Fe₂O₃(s) + SO₂(g) + SO₃(g)
                    3. **Displacement Reaction**: More reactive element displaces less reactive element.
                       Fe(s) + CuSO₄(aq) → FeSO₄(aq) + Cu(s)
                    4. **Double Displacement Reaction**: Exchange of ions between reactants.
                       Na₂SO₄(aq) + BaCl₂(aq) → BaSO₄(s) [White ppt] + 2NaCl(aq)
                    5. **Oxidation and Reduction (Redox)**:
                       - Oxidation: Gain of Oxygen or Loss of Hydrogen.
                       - Reduction: Loss of Oxygen or Gain of Hydrogen.
                """.trimIndent(),
                authorName = "Mrs. Sunita Verma",
                isApproved = true,
                dateAdded = "2024-10-18",
                isBookmarked = false,
                formatType = "PDF Summary"
            ),
            StudyNoteEntity(
                title = "Hindi Vyakaran: Sangya, Sarvanam aur Sandhi",
                classNumber = 9,
                subject = "Hindi",
                chapterNumber = 3,
                chapterName = "व्याकरण बोध",
                topic = "संज्ञा के भेद, सर्वनाम एवं स्वर संधि के नियम",
                content = """
                    ## हिंदी व्याकरण - प्रमुख नियम
                    
                    ### संज्ञा (Noun):
                    किसी व्यक्ति, वस्तु, स्थान, गुण या भाव के नाम को संज्ञा कहते हैं।
                    **संज्ञा के मुख्य तीन भेद होते हैं:**
                    1. व्यक्तिवाचक संज्ञा (जैसे: हिमालय, गंगा, राम)
                    2. जातिवाचक संज्ञा (जैसे: नदी, पर्वत, बालक)
                    3. भाववाचक संज्ञा (जैसे: मिठास, बचपन, ईमानदारी)
                    
                    ### सर्वनाम (Pronoun):
                    संज्ञा के स्थान पर प्रयुक्त होने वाले शब्दों को सर्वनाम कहते हैं।
                    **भेद (6):**
                    - पुरुषवाचक (मैं, तुम, वह)
                    - निश्चयवाचक (यह, वह)
                    - अनिश्चयवाचक (कोई, कुछ)
                    - संबंधवाचक (जो-सो, जिसका)
                    - प्रश्नवाचक (कौन, क्या)
                    - निजवाचक (स्वयं, अपने आप)
                    
                    ### स्वर संधि (Swar Sandhi):
                    दो स्वरों के मेल से होने वाले विकार को स्वर संधि कहते हैं।
                    (दीर्घ संधि, गुण संधि, वृद्धि संधि, यण संधि, अयादि संधि)
                """.trimIndent(),
                authorName = "Mr. Vikramaditya Singh",
                isApproved = true,
                dateAdded = "2024-10-22",
                isBookmarked = true,
                formatType = "Notes"
            ),
            StudyNoteEntity(
                title = "Cell: The Fundamental Unit of Life",
                classNumber = 9,
                subject = "Science",
                chapterNumber = 5,
                chapterName = "Cell Biology",
                topic = "Prokaryotic vs Eukaryotic Cells & Cell Organelles",
                content = """
                    ## Chapter 5: Fundamental Unit of Life
                    
                    ### Discovery:
                    - Robert Hooke (1665) observed cork cells under crude microscope.
                    - Anton van Leeuwenhoek (1674) observed free-living cells.
                    - Robert Brown (1831) discovered Nucleus.
                    
                    ### Cell Organelles:
                    1. **Mitochondria**: 'Powerhouse of the cell' - generates ATP.
                    2. **Endoplasmic Reticulum (ER)**: RER (Protein synthesis) and SER (Lipid synthesis).
                    3. **Golgi Apparatus**: Packaging and modification of molecules.
                    4. **Lysosomes**: 'Suicide bags' of the cell.
                    5. **Plastids**: Found only in plant cells (Chloroplasts, Chromoplasts, Leucoplasts).
                """.trimIndent(),
                authorName = "Mrs. Sunita Verma",
                isApproved = true,
                dateAdded = "2024-10-25",
                isBookmarked = false,
                formatType = "Notes"
            ),
            StudyNoteEntity(
                title = "Light - Reflection and Refraction Complete Guide",
                classNumber = 10,
                subject = "Science",
                chapterNumber = 10,
                chapterName = "Light",
                topic = "Mirror Formula, Lens Formula and Sign Conventions",
                content = """
                    ## Chapter 10: Light - Reflection and Refraction
                    
                    ### Laws of Reflection:
                    1. Angle of incidence equals angle of reflection (∠i = ∠r).
                    2. Incident ray, reflected ray, and normal at point of incidence all lie in the same plane.
                    
                    ### Mirror Formula:
                    1/f = 1/v + 1/u
                    Magnification: m = -v/u = h'/h
                    
                    ### Lens Formula:
                    1/f = 1/v - 1/u
                    Magnification: m = v/u = h'/h
                    
                    ### Power of a Lens:
                    P = 1 / f (in metres), unit is Dioptre (D).
                """.trimIndent(),
                authorName = "Mrs. Sunita Verma",
                isApproved = true,
                dateAdded = "2024-10-28",
                isBookmarked = false,
                formatType = "Notes"
            )
        )
        dao.insertNotes(notes)

        // 8. Seed Notices
        val notices = listOf(
            NoticeEntity(
                title = "Annual Sports Day 2024 Announcement",
                content = "M.I.C.D School will celebrate its Annual Sports Meet on November 25, 2024. All track and field events registrations are open with respective Physical Education teachers.",
                category = "General",
                date = "2024-11-01",
                author = "Principal Office",
                isUrgent = false
            ),
            NoticeEntity(
                title = "Half-Yearly Examination Results Published",
                content = "The academic results for Half-Yearly Examinations (Classes 1 to 12) have been officially published on the school portal. Parents and students can view subject-wise marksheets and rank cards.",
                category = "Result",
                date = "2024-10-30",
                author = "Examination Controller",
                isUrgent = true
            ),
            NoticeEntity(
                title = "Parent-Teacher Meeting (PTM) Schedule",
                content = "Parent-Teacher Meeting for Classes 1 to 12 is scheduled for Saturday, November 9, 2024, from 9:00 AM to 1:00 PM. Parents are cordially invited to review their ward's academic progress.",
                category = "Parent Meeting",
                date = "2024-11-02",
                author = "Academic Dean",
                isUrgent = true
            ),
            NoticeEntity(
                title = "Science Exhibition & AI Innovation Fair",
                content = "Students from Classes 6 to 12 are invited to submit working models, robotics projects, and AI software ideas for the M.I.C.D Annual Science Exhibition by November 15.",
                category = "General",
                date = "2024-11-03",
                author = "Science Department",
                isUrgent = false
            ),
            NoticeEntity(
                title = "Winter Uniform Mandatory from November 15",
                content = "All students must report in official winter school uniform starting November 15, 2024. Blazers and navy blue sweaters with school crest are compulsory.",
                category = "Holiday",
                date = "2024-11-04",
                author = "Discipline Incharge",
                isUrgent = false
            )
        )
        dao.insertNotices(notices)

        // 9. Seed Weekly Timetable for Class 10-A
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        val periods = listOf(
            Triple(1, "08:00 AM", "08:45 AM") to Pair("Mathematics", "Mr. Rajesh Sharma"),
            Triple(2, "08:45 AM", "09:30 AM") to Pair("Science", "Mrs. Sunita Verma"),
            Triple(3, "09:30 AM", "10:15 AM") to Pair("English", "Mrs. Priya Nair"),
            Triple(4, "10:45 AM", "11:30 AM") to Pair("Hindi", "Mr. Vikramaditya Singh"),
            Triple(5, "11:30 AM", "12:15 PM") to Pair("Social Science", "Dr. Anand Joshi"),
            Triple(6, "12:15 PM", "01:00 PM") to Pair("Computer Science", "Mrs. Neha Gupta"),
            Triple(7, "01:30 PM", "02:15 PM") to Pair("Sports / Library", "Physical Trainer")
        )

        val timetableEntries = mutableListOf<TimetableEntity>()
        for (day in days) {
            for ((periodTiming, subjectTeacher) in periods) {
                timetableEntries.add(
                    TimetableEntity(
                        classNumber = 10,
                        section = "A",
                        dayOfWeek = day,
                        periodNumber = periodTiming.first,
                        startTime = periodTiming.second,
                        endTime = periodTiming.third,
                        subject = subjectTeacher.first,
                        teacherName = subjectTeacher.second
                    )
                )
            }
        }
        dao.insertTimetableEntries(timetableEntries)

        // 10. Seed Quizzes
        val quizzes = listOf(
            QuizEntity(
                quizId = "QUIZ-101",
                title = "Quadratic Equations Speed Challenge",
                subject = "Mathematics",
                classNumber = 10,
                deadlineDate = "2024-11-10",
                totalMarks = 25,
                status = "PUBLISHED"
            ),
            QuizEntity(
                quizId = "QUIZ-102",
                title = "Light & Ray Optics Quiz",
                subject = "Science",
                classNumber = 10,
                deadlineDate = "2024-11-12",
                totalMarks = 20,
                status = "PUBLISHED"
            ),
            QuizEntity(
                quizId = "QUIZ-103",
                title = "Hindi Grammar - Sandhi & Samas",
                subject = "Hindi",
                classNumber = 9,
                deadlineDate = "2024-11-15",
                totalMarks = 30,
                status = "UPCOMING"
            )
        )
        dao.insertQuizzes(quizzes)

        // 11. Seed Competitions
        val competitions = listOf(
            CompetitionEntity(
                competitionId = "COMP-2024-01",
                title = "All-India Science & AI Innovation Fair 2024",
                category = "Science Fair",
                eligibleClasses = "6,7,8,9,10,11,12",
                registrationDeadline = "2024-11-14",
                eventDate = "2024-11-20",
                status = "REGISTRATION_OPEN"
            ),
            CompetitionEntity(
                competitionId = "COMP-2024-02",
                title = "M.I.C.D Inter-Class Math Olympiad",
                category = "Math Olympiad",
                eligibleClasses = "8,9,10,11,12",
                registrationDeadline = "2024-11-08",
                eventDate = "2024-11-11",
                status = "STARTING_SOON"
            ),
            CompetitionEntity(
                competitionId = "COMP-2024-03",
                title = "Junior Debate & Elocution Championship",
                category = "Debate",
                eligibleClasses = "6,7,8,9,10",
                registrationDeadline = "2024-10-25",
                eventDate = "2024-10-30",
                status = "RESULTS_PUBLISHED",
                winnerStudentName = "Aarav Sharma (Class 10-A)"
            )
        )
        dao.insertCompetitions(competitions)

        // 12. Seed Notifications
        val notifications = listOf(
            NotificationRecordEntity(
                notificationId = "NOTIF-001",
                title = "📢 Half-Yearly Results Officially Published",
                message = "Half-Yearly Examination 2024 results are live! Check your subject-wise marks and class rank in the Student and Parent portals.",
                channelId = "examination_notices",
                targetRole = "ALL",
                targetClass = 0,
                targetSection = "ALL",
                targetUrl = "micdschool://results",
                senderRole = "PRINCIPAL_ADMIN",
                senderName = "Principal Dr. K. N. Verma",
                timestamp = System.currentTimeMillis() - 3600000 * 5,
                status = "DELIVERED",
                isRead = false
            ),
            NotificationRecordEntity(
                notificationId = "NOTIF-002",
                title = "📝 Mathematics Homework: Ex 4.2 Assigned",
                message = "Class 10-A: Complete Quadratic Equations exercise 4.2 questions 1-10 before November 6.",
                channelId = "homework_updates",
                targetRole = "STUDENTS",
                targetClass = 10,
                targetSection = "A",
                targetUrl = "micdschool://homework",
                senderRole = "TEACHER",
                senderName = "Mr. Rajesh Sharma",
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                status = "DELIVERED",
                isRead = true
            ),
            NotificationRecordEntity(
                notificationId = "NOTIF-003",
                title = "🏆 Science Fair 2024 Registration Open",
                message = "Registration for M.I.C.D Innovation Fair is now open for Classes 6-12. Submit working models by Nov 14.",
                channelId = "competition_notifications",
                targetRole = "ALL",
                targetClass = 0,
                targetSection = "ALL",
                targetUrl = "micdschool://competitions",
                senderRole = "SUPER_ADMIN",
                senderName = "Super Admin Office",
                timestamp = System.currentTimeMillis() - 3600000 * 1,
                status = "DELIVERED",
                isRead = false
            ),
            NotificationRecordEntity(
                notificationId = "NOTIF-004",
                title = "⚡ New Quiz: Quadratic Equations Speed Challenge",
                message = "A 25-mark speed quiz has been published for Class 10. Deadline: November 10.",
                channelId = "quiz_notifications",
                targetRole = "STUDENTS",
                targetClass = 10,
                targetSection = "ALL",
                targetUrl = "micdschool://quizzes",
                senderRole = "PRINCIPAL_ADMIN",
                senderName = "Exam Coordinator",
                timestamp = System.currentTimeMillis() - 1800000,
                status = "DELIVERED",
                isRead = false
            )
        )
        dao.insertNotifications(notifications)

        // 13. Seed Video Lectures
        val videoLectures = listOf(
            VideoLectureEntity(
                id = 1,
                title = "Quadratic Equations: Complete Concept & Shortcuts",
                subject = "Mathematics",
                classNumber = 10,
                chapterNumber = 4,
                chapterName = "Quadratic Equations",
                teacherName = "Mr. Rajesh Sharma",
                durationMinutes = 45,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "",
                description = "Master quadratic equations with factoring, completing the square, quadratic formula, and discriminant nature of roots with 10 board exam problems.",
                uploadDate = "2024-11-01",
                viewCount = 286,
                hasNotes = true
            ),
            VideoLectureEntity(
                id = 2,
                title = "Chemical Reactions & Equations - Full Chapter Animation",
                subject = "Science",
                classNumber = 10,
                chapterNumber = 1,
                chapterName = "Chemical Reactions & Equations",
                teacherName = "Dr. Anand Joshi",
                durationMinutes = 52,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnailUrl = "",
                description = "Balancing chemical equations, combination, decomposition, displacement, double displacement, redox reactions, corrosion and rancidity explained with animations.",
                uploadDate = "2024-11-02",
                viewCount = 342,
                hasNotes = true
            ),
            VideoLectureEntity(
                id = 3,
                title = "Newton's Laws of Motion & Momentum with Practical Demo",
                subject = "Physics",
                classNumber = 9,
                chapterNumber = 9,
                chapterName = "Force and Laws of Motion",
                teacherName = "Mrs. Sunita Verma",
                durationMinutes = 38,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUrl = "",
                description = "Inertia, F=ma calculation, action-reaction pairs, and conservation of momentum with laboratory demonstration and numerical problems.",
                uploadDate = "2024-11-03",
                viewCount = 195,
                hasNotes = true
            ),
            VideoLectureEntity(
                id = 4,
                title = "Tenses Masterclass: Active & Passive Voice in English",
                subject = "English",
                classNumber = 10,
                chapterNumber = 2,
                chapterName = "English Grammar & Writing",
                teacherName = "Mrs. Priya Nair",
                durationMinutes = 35,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnailUrl = "",
                description = "Clear all confusion in Present, Past, and Future tenses with rule chart, conversion shortcuts, and practice exercises for CBSE/State exams.",
                uploadDate = "2024-11-04",
                viewCount = 210,
                hasNotes = true
            ),
            VideoLectureEntity(
                id = 5,
                title = "Light: Reflection, Refraction & Lens Formula",
                subject = "Science",
                classNumber = 10,
                chapterNumber = 10,
                chapterName = "Light - Reflection and Refraction",
                teacherName = "Mrs. Sunita Verma",
                durationMinutes = 60,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnailUrl = "",
                description = "Ray diagrams for concave/convex mirrors and lenses, sign convention rules, mirror formula, and refractive index problem solving.",
                uploadDate = "2024-11-05",
                viewCount = 412,
                hasNotes = true
            ),
            VideoLectureEntity(
                id = 6,
                title = "Hindi Vyakaran: Sandhi & Samas (संधि एवं समास)",
                subject = "Hindi",
                classNumber = 10,
                chapterNumber = 3,
                chapterName = "व्याकरण बोध",
                teacherName = "Dr. Anand Joshi",
                durationMinutes = 40,
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
                thumbnailUrl = "",
                description = "स्वर, व्यंजन एवं विसर्ग संधि के नियम तथा तत्पुरुष, कर्मधारय, बहुव्रीहि समास की पहचान के सरल सूत्र।",
                uploadDate = "2024-11-04",
                viewCount = 188,
                hasNotes = true
            )
        )
        dao.insertVideoLectures(videoLectures)

        // 14. Seed Peer & Teacher Chat Messages
        val chatMessages = listOf(
            PeerChatMessageEntity(
                senderId = "MICD-STU-001",
                senderName = "Aryan Sharma",
                senderRole = "STUDENT",
                recipientId = "MICD-STU-002",
                recipientName = "Priya Patel",
                message = "Hey Priya! Did you finish solving Exercise 4.2 in Quadratic Equations?",
                classNumber = 10,
                section = "A",
                timestamp = System.currentTimeMillis() - 7200000,
                isRead = true,
                chatType = "STUDENT_TO_STUDENT"
            ),
            PeerChatMessageEntity(
                senderId = "MICD-STU-002",
                senderName = "Priya Patel",
                senderRole = "STUDENT",
                recipientId = "MICD-STU-001",
                recipientName = "Aryan Sharma",
                message = "Yes Aryan! Question 5 was tricky, but I used the quadratic formula. I can share the steps with you.",
                classNumber = 10,
                section = "A",
                timestamp = System.currentTimeMillis() - 5400000,
                isRead = true,
                chatType = "STUDENT_TO_STUDENT"
            ),
            PeerChatMessageEntity(
                senderId = "MICD-STU-001",
                senderName = "Aryan Sharma",
                senderRole = "STUDENT",
                recipientId = "TCH-001",
                recipientName = "Mr. Rajesh Sharma",
                message = "Respected Sir, can you please explain the derivation of discriminant condition D < 0 tomorrow in class?",
                classNumber = 10,
                section = "A",
                timestamp = System.currentTimeMillis() - 3600000,
                isRead = true,
                chatType = "STUDENT_TO_TEACHER"
            ),
            PeerChatMessageEntity(
                senderId = "TCH-001",
                senderName = "Mr. Rajesh Sharma",
                senderRole = "TEACHER",
                recipientId = "MICD-STU-001",
                recipientName = "Aryan Sharma",
                message = "Sure Aryan! I will demonstrate it on the smartboard during the second period. Please review page 74 tonight.",
                classNumber = 10,
                section = "A",
                timestamp = System.currentTimeMillis() - 1800000,
                isRead = true,
                chatType = "STUDENT_TO_TEACHER"
            ),
            PeerChatMessageEntity(
                senderId = "MICD-STU-003",
                senderName = "Rohit Verma",
                senderRole = "STUDENT",
                recipientId = "ALL_CLASS_10A",
                recipientName = "Class 10-A Group",
                message = "Don't forget tomorrow is Science practical file submission day everyone!",
                classNumber = 10,
                section = "A",
                timestamp = System.currentTimeMillis() - 900000,
                isRead = false,
                chatType = "CLASS_GROUP"
            )
        )
        dao.insertChatMessages(chatMessages)

        // 15. Seed Student Applications (Leave & Permission to Sir)
        val applications = listOf(
            StudentApplicationEntity(
                applicationId = "APP-2024-001",
                studentId = "MICD-STU-001",
                studentName = "Aryan Sharma",
                classNumber = 10,
                section = "A",
                rollNumber = 1,
                applicationType = "LEAVE",
                subject = "Application for 2 Days Sick Leave",
                reason = "Respected Class Teacher Sir, I am suffering from viral fever and doctor has advised 2 days bed rest. Kindly grant me leave for 6th and 7th November.",
                startDate = "2024-11-06",
                endDate = "2024-11-07",
                submittedDate = "2024-11-05",
                status = "APPROVED",
                teacherRemarks = "Approved. Take care of your health and collect missed notes from Priya.",
                reviewedBy = "Mr. Rajesh Sharma Sir",
                reviewedDate = "2024-11-05"
            ),
            StudentApplicationEntity(
                applicationId = "APP-2024-002",
                studentId = "MICD-STU-001",
                studentName = "Aryan Sharma",
                classNumber = 10,
                section = "A",
                rollNumber = 1,
                applicationType = "GENERAL",
                subject = "Permission to Participate in State Science Olympiad",
                reason = "Respected Principal Sir, I have qualified for the State Level Science Olympiad round. Kindly grant permission to attend the training workshop on Friday.",
                startDate = "2024-11-08",
                endDate = "2024-11-08",
                submittedDate = "2024-11-04",
                status = "APPROVED",
                teacherRemarks = "Permission granted. Best of luck for the Olympiad!",
                reviewedBy = "Principal Office",
                reviewedDate = "2024-11-04"
            ),
            StudentApplicationEntity(
                applicationId = "APP-2024-003",
                studentId = "MICD-STU-002",
                studentName = "Priya Patel",
                classNumber = 10,
                section = "A",
                rollNumber = 2,
                applicationType = "BONAFIDE",
                subject = "Request for Bonafide Student Certificate",
                reason = "Respected Sir, I require a bonafide certificate for passport verification purpose. Please issue the certificate at the earliest.",
                startDate = "",
                endDate = "",
                submittedDate = "2024-11-05",
                status = "PENDING",
                teacherRemarks = "",
                reviewedBy = "",
                reviewedDate = ""
            )
        )
        dao.insertApplications(applications)

        // 16. Seed Default Registered User Account for Instant Login
        val demoAccounts = listOf(
            UserAccountEntity(
                id = 1,
                studentName = "Aryan Sharma",
                rollNumber = 1,
                classNumber = 10,
                section = "A",
                mobileNumber = "9876543210",
                parentsName = "Rajesh Sharma",
                password = "password123",
                role = "STUDENT",
                isLoggedIn = true,
                registeredDate = System.currentTimeMillis() - 86400000 * 30
            )
        )
        dao.insertUserAccounts(demoAccounts)
    }
}
