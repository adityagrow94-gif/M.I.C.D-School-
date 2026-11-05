package com.example.ai

import kotlinx.coroutines.delay

enum class AiLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    HINDI("हिन्दी (Hindi)", "hi"),
    HINGLISH("Hinglish", "hinglish")
}

data class GeneratedStudyResource(
    val title: String,
    val summary: String,
    val keyPoints: List<String>,
    val mcqs: List<GeneratedMcq>,
    val shortQuestions: List<String>,
    val longQuestions: List<String>
)

data class GeneratedMcq(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

object SchoolAiTeacherService {

    /**
     * Responds as M.I.C.D AI TEACHER adapting to class level, language, and curriculum topics.
     */
    suspend fun getAiAnswer(
        query: String,
        classLevel: Int,
        language: AiLanguage
    ): String {
        delay(600) // Realistic processing delay
        val lower = query.lowercase().trim()

        return when {
            // Mathematics
            lower.contains("math") || lower.contains("quadratic") || lower.contains("trigonometry") ||
            lower.contains("formula") || lower.contains("equation") || lower.contains("solve") || lower.contains("गणित") -> {
                generateMathExplanation(query, classLevel, language)
            }

            // Science / Physics / Chemistry / Biology
            lower.contains("science") || lower.contains("newton") || lower.contains("cell") ||
            lower.contains("light") || lower.contains("chemical") || lower.contains("acid") ||
            lower.contains("gravity") || lower.contains("विज्ञान") -> {
                generateScienceExplanation(query, classLevel, language)
            }

            // Hindi Grammar
            lower.contains("hindi") || lower.contains("vyakaran") || lower.contains("sangya") ||
            lower.contains("sarvanam") || lower.contains("sandhi") || lower.contains("व्याकरण") || lower.contains("संज्ञा") -> {
                generateHindiGrammarExplanation(query, classLevel, language)
            }

            // English Grammar
            lower.contains("english") || lower.contains("grammar") || lower.contains("tense") ||
            lower.contains("passive") || lower.contains("active") || lower.contains("noun") || lower.contains("verb") -> {
                generateEnglishGrammarExplanation(query, classLevel, language)
            }

            // Practice Questions / Quiz
            lower.contains("practice") || lower.contains("mcq") || lower.contains("test") ||
            lower.contains("quiz") || lower.contains("question") || lower.contains("प्रश्न") -> {
                generatePracticeQuestions(query, classLevel, language)
            }

            // General Educational Inquiry
            else -> {
                generateGeneralEducationalResponse(query, classLevel, language)
            }
        }
    }

    private fun generateMathExplanation(query: String, classLevel: Int, lang: AiLanguage): String {
        return when (lang) {
            AiLanguage.HINDI -> """
                🎓 **एम.आई.सी.डी एआई शिक्षक (कक्षा $classLevel गणित मार्गदर्शन)**
                
                नमस्ते! आपके गणित के प्रश्न पर चरणबद्ध समाधान:
                
                📌 **मुख्य संकल्पना (Concept):**
                किसी भी समीकरण को हल करने के लिए नियमों का क्रमबद्ध पालन आवश्यक है। 
                
                📝 **चरणबद्ध समाधान (Step-by-Step Solution):**
                1. **चरण 1:** दिए गए समीकरण की मानक रूप ($classLevel वीं कक्षा स्तर) से तुलना करें।
                2. **चरण 2:** अज्ञात चर (x या y) के गुणांकों को पहचानें।
                3. **चरण 3:** उपयुक्त सूत्र (जैसे द्विघात सूत्र x = (-b ± √(b² - 4ac)) / 2a अथवा त्रिकोणमितीय सर्वसमिका) लागू करें।
                4. **चरण 4:** गणना करें और अंतिम उत्तर की शुद्धता जांचें।
                
                💡 **शिक्षक सुझाव:** परीक्षा में रफ कार्य के लिए दाईं ओर अलग मार्जिन बनाएं। क्या आप इस विषय पर अभ्यास प्रश्न हल करना चाहते हैं?
            """.trimIndent()

            AiLanguage.HINGLISH -> """
                🎓 **M.I.C.D AI TEACHER (Class $classLevel Math Help)**
                
                Hello student! Ye raha aapke Math query ka step-by-step breakdown:
                
                📌 **Main Concept:**
                Class $classLevel ke syllabus ke according, is tarah ke problems ko systematic steps me solve karna sabse easy aur scoring hota hai.
                
                📝 **Step-by-Step Method:**
                1. **Step 1 - Identify given data:** Question me kya diya hai aur kya find karna hai use clearly likhein.
                2. **Step 2 - Formula Selection:** Standard formula apply karein (e.g. Factorisation method ya Trigonometric identity $\sin^2\theta + \cos^2\theta = 1$).
                3. **Step 3 - Simplification:** Signs (+/-) ka dhyan rakhte hue bracket solve karein.
                4. **Step 4 - Final Answer:** Units (cm, m², units) likhna kabhi na bhoolein!
                
                💡 **Pro Tip:** Daily 5 questions practice karein speed aur accuracy badhane ke liye.
            """.trimIndent()

            AiLanguage.ENGLISH -> """
                🎓 **M.I.C.D AI TEACHER (Class $classLevel Mathematics)**
                
                Welcome to your interactive math session!
                
                📌 **Core Concept & Principle:**
                Tailored for Class $classLevel curriculum standards. Precision and structured deduction are key.
                
                📝 **Step-by-Step Educational Solution:**
                • **Step 1 (Given & Required):** Clearly define variables and constants extracted from the problem.
                • **Step 2 (Formula Application):** State the governing mathematical theorem or identity clearly before substitution.
                • **Step 3 (Algebraic Evaluation):** Execute arithmetic calculations step by step to avoid sign errors.
                • **Step 4 (Verification):** Substitute the calculated root back into the original expression to verify consistency.
                
                💡 **Teacher's Advice:** Always highlight your final answer in a neat box. Would you like 3 sample practice problems on this chapter?
            """.trimIndent()
        }
    }

    private fun generateScienceExplanation(query: String, classLevel: Int, lang: AiLanguage): String {
        return when (lang) {
            AiLanguage.HINDI -> """
                🔬 **एम.आई.सी.डी एआई शिक्षक (कक्षा $classLevel विज्ञान पाठ समझाव)**
                
                📌 **अध्याय सारांश और वैज्ञानिक सिद्धांत:**
                विज्ञान को समझने का सबसे अच्छा तरीका चित्रों और दैनिक जीवन के उदाहरणों से जोड़ना है।
                
                🧪 **मुख्य बिंदु:**
                1. **परिभाषा:** वैज्ञानिक नियमों को सटीक शब्दों में याद रखें।
                2. **दैनिक जीवन का उदाहरण:** क्रिया और प्रतिक्रिया (न्यूटन का तीसरा नियम) जैसे नाव से कूदते समय नाव का पीछे जाना।
                3. **प्रयोग और प्रेक्षण:** परीक्षा में नामांकित चित्र (Labelled Diagram) अनिवार्य रूप से बनाएं।
                
                ✨ **याद रखने योग्य सूत्र:** बल = द्रव्यमान × त्वरण (F = m × a)
            """.trimIndent()

            AiLanguage.HINGLISH -> """
                🔬 **M.I.C.D AI TEACHER (Class $classLevel Science Explainer)**
                
                📌 **Topic Overview:**
                Science concepts ko ratne ki jagah visualize karna chahiye!
                
                🧪 **Key Takeaways:**
                1. **Definition:** Scientific definitions me standard keywords zaroor mention karein.
                2. **Real World Example:** Real life application jaise mirrors in car headlights ya photosynthesis in leaves.
                3. **Diagrams:** Clean ray diagrams aur biological cell diagrams banakar label karein full marks paane ke liye.
                
                ✨ **Revision Tip:** Har chapter ke end me diye gaye NCERT summary points ko 2 baar revise karein.
            """.trimIndent()

            AiLanguage.ENGLISH -> """
                🔬 **M.I.C.D AI TEACHER (Class $classLevel Science Concept Guide)**
                
                📌 **Theoretical Foundation:**
                Aligned with CBSE/ICSE curriculum for Class $classLevel.
                
                🧪 **Key Explanatory Pillars:**
                1. **Fundamental Laws:** Understand cause and effect relationships (e.g., energy conservation, chemical bonding principles).
                2. **Experimental Evidence:** Observations, hypothesis testing, and laboratory safety conventions.
                3. **Formula & Units:** Always accompany physical quantities with correct SI units (Joules, Watts, Newtons, Pascals).
                
                ✨ **Exam Tip:** Draw neat, pencil-shaded labelled diagrams in 3-mark and 5-mark questions for maximum scoring.
            """.trimIndent()
        }
    }

    private fun generateHindiGrammarExplanation(query: String, classLevel: Int, lang: AiLanguage): String {
        return """
            📚 **एम.आई.सी.डी एआई शिक्षक (कक्षा $classLevel - हिंदी व्याकरण)**
            
            📌 **संज्ञा, सर्वनाम एवं संधि के सरल नियम:**
            
            1. **संज्ञा:** किसी व्यक्ति, वस्तु, स्थान, भाव या अवस्था के नाम को संज्ञा कहते हैं।
               - **व्यक्तिवाचक:** विशेष नाम (उदा. हिमालय, महात्मा गांधी, भारत)
               - **जातिवाचक:** पूरी जाति का बोध (उदा. नदी, वृक्ष, पुस्तक)
               - **भाववाचक:** अनुभव किए जाने वाले भाव (उदा. मिठास, बचपन, वीरता)
               
            2. **सर्वनाम:** संज्ञा के बदले आने वाले शब्द (मैं, तुम, आप, वह, यह)।
               - इसके 6 भेद होते हैं (पुरुषवाचक, निश्चयवाचक, अनिश्चयवाचक, संबंधवाचक, प्रश्नवाचक, निजवाचक)।
               
            3. **स्वर संधि का मूल नियम:**
               - **दीर्घ संधि:** अ + अ = आ (हिम + आलय = हिमालय)
               - **गुण संधि:** अ + इ = ए (नर + इंद्र = नरेंद्र)
               
            💡 **अभ्यास सुझाव:** प्रतिदिन 5 वाक्यों में संज्ञा और सर्वनाम छांटने का अभ्यास करें!
        """.trimIndent()
    }

    private fun generateEnglishGrammarExplanation(query: String, classLevel: Int, lang: AiLanguage): String {
        return """
            📖 **M.I.C.D AI TEACHER (Class $classLevel - English Grammar)**
            
            📌 **Essential Grammar Rules & Usage:**
            
            1. **Active and Passive Voice:**
               - *Active:* Subject does the action. (e.g., "The teacher praised Rohan.")
               - *Passive:* Object becomes the focus. (e.g., "Rohan was praised by the teacher.")
               - *Rule:* Object + Auxiliary verb + V3 (Past Participle) + by + Subject.
               
            2. **Tenses Overview:**
               - *Present Continuous:* Subject + is/am/are + V-ing ("She is reading.")
               - *Present Perfect:* Subject + has/have + V3 ("They have completed the project.")
               
            3. **Subject-Verb Agreement:**
               - Singular subject requires singular verb ("The team is ready.")
               - Plural subject requires plural verb ("The students are playing.")
               
            💡 **Grammar Tip:** Proofread your essays for subject-verb agreement and punctuation before final submission!
        """.trimIndent()
    }

    private fun generatePracticeQuestions(query: String, classLevel: Int, lang: AiLanguage): String {
        return """
            📝 **M.I.C.D AI Practice Quiz - Class $classLevel**
            
            **Question 1 (Multiple Choice):**
            Which organelle is universally known as the powerhouse of the cell?
            A) Golgi Body
            B) Mitochondria
            C) Lysosome
            D) Ribosome
            *Answer: B (Mitochondria synthesizes ATP).*
            
            **Question 2 (Short Answer):**
            State Ohm's Law and write its mathematical equation with units.
            *Key: V = I * R, where V is Voltage (Volts), I is Current (Amperes), and R is Resistance (Ohms).*
            
            **Question 3 (Grammar):**
            Identify the tense in: *"The principal had addressed the morning assembly before classes began."*
            *Answer: Past Perfect Tense.*
            
            🎯 *Tap 'New Chat' or ask for more subject-specific practice questions anytime!*
        """.trimIndent()
    }

    private fun generateGeneralEducationalResponse(query: String, classLevel: Int, lang: AiLanguage): String {
        return when (lang) {
            AiLanguage.HINDI -> """
                🎓 **एम.आई.सी.डी एआई शिक्षक** (कक्षा $classLevel के लिए)
                
                नमस्ते! मैं आपका डिजिटल शिक्षक हूँ। मैं आपकी निम्नलिखित विषयों में सहायता कर सकता हूँ:
                
                • 📐 **गणित:** सूत्रों को समझना और प्रश्नों को चरण-दर-चरण हल करना।
                • 🔬 **विज्ञान:** भौतिकी, रसायन और जीव विज्ञान के कठिन अध्यायों को आसान भाषा में समझना।
                • 📖 **भाषा एवं व्याकरण:** हिन्दी और अंग्रेजी व्याकरण के नियम, निबंध और पत्र लेखन।
                • 📝 **परीक्षा तैयारी:** महत्वपूर्ण प्रश्नों की सूची, बहुविकल्पीय प्रश्न (MCQs) और अभ्यास पत्र।
                
                कृपया अपना विशिष्ट प्रश्न या अध्याय का नाम लिखकर भेजें!
            """.trimIndent()

            AiLanguage.HINGLISH -> """
                🎓 **M.I.C.D AI TEACHER** (For Class $classLevel)
                
                Hello student! Mai aapka personalized AI Teacher hoon. Aap mujhse kisi bhi subject ke questions pooch sakte hain:
                
                • 📐 **Maths:** Step-by-step problem solving & formula derivations.
                • 🔬 **Science:** Easy explanations with real-world examples & diagrams guidance.
                • 📖 **Hindi & English:** Grammar rules, translations, and essay outlines.
                • 📝 **Homework & Exams:** Practice MCQs, revision summaries & sample answers.
                
                Bataiye, aaj kis chapter ya topic me help chahiye?
            """.trimIndent()

            AiLanguage.ENGLISH -> """
                🎓 **M.I.C.D AI TEACHER** (Class $classLevel Learning Assistant)
                
                Welcome to M.I.C.D School's AI Learning Hub! I am equipped to support your academic journey across all subjects:
                
                • 📐 **Mathematics:** Conceptual proofs, formula applications, and step-by-step equations.
                • 🔬 **Science (Physics, Chemistry, Biology):** Chapter breakdowns, laws, and experimental insights.
                • 📖 **Languages:** Grammar mechanics, comprehension, and vocabulary enrichment.
                • 📝 **Exam Prep:** Revision notes, chapter summaries, and timed practice question sets.
                
                Type your question above or choose one of the suggested prompts to begin!
            """.trimIndent()
        }
    }

    /**
     * AI Notes Generator for Teachers & Admin
     */
    suspend fun generateStudyResource(
        subject: String,
        classLevel: Int,
        chapter: String
    ): GeneratedStudyResource {
        delay(700)
        return GeneratedStudyResource(
            title = "Comprehensive Study Pack: $chapter ($subject - Class $classLevel)",
            summary = "This high-yield revision pack for Class $classLevel $subject covers foundational principles, key formulas, critical diagram notations, and board examination patterns for $chapter.",
            keyPoints = listOf(
                "Core definitions and governing laws of $chapter explained with precision.",
                "High-frequency board examination formulas and standard units.",
                "Common student misconceptions and examiner-recommended corrections.",
                "Real-world engineering and scientific applications."
            ),
            mcqs = listOf(
                GeneratedMcq(
                    question = "What is the primary governing principle of this topic in Class $classLevel $subject?",
                    options = listOf("Conservation Law", "Random Fluctuation", "Static Equilibrium", "Dispersive Motion"),
                    correctIndex = 0,
                    explanation = "Conservation of fundamental quantities underlies all predictive models in this chapter."
                ),
                GeneratedMcq(
                    question = "Which unit is internationally recognized as standard (SI) for calculations here?",
                    options = listOf("CGS Unit", "SI Metric Standard", "Imperial Unit", "Dimensionless Ratio"),
                    correctIndex = 1,
                    explanation = "SI units must be universally adopted in numerical solutions."
                ),
                GeneratedMcq(
                    question = "When solving 3-mark analytical problems, what is the recommended starting step?",
                    options = listOf("Guessing the answer", "Listing Given Data & Governing Formula", "Skipping calculation", "Writing essay"),
                    correctIndex = 1,
                    explanation = "Explicitly stating given variables and formula guarantees partial credit in evaluations."
                )
            ),
            shortQuestions = listOf(
                "Define the key terms related to $chapter and state their mathematical formulations.",
                "Differentiate between theoretical predictions and experimental observations in this domain.",
                "Give two real-life industrial or everyday examples illustrating this concept."
            ),
            longQuestions = listOf(
                "Explain the comprehensive derivation and experimental setup for $chapter with a labelled schematic diagram.",
                "Analyze a numerical case study where variables double in magnitude and explain the resultant effect on the system."
            )
        )
    }
}
