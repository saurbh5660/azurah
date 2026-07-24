package com.live.azurah.model

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val context: String = "This answer follows Esther's story and the courage shown through faith and obedience.",
    val verse: String = "Esther"
)

object BibleQuizData {
    val questions = listOf(
        QuizQuestion(
            question = "Why did Esther initially hesitate before approaching King Ahasuerus?",
            options = listOf(
                "She was afraid of public speaking",
                "Anyone who entered the king's court unsummoned risked death",
                "She did not believe Mordecai's warning",
                "She had not fasted and prayed as required"
            ),
            correctIndex = 1,
            context = "Under Persian law, approaching the king unsummoned was punishable by death — even for the queen. Esther's courage wasn't the absence of fear — it was choosing faith over it.",
            verse = "Esther 4:11"
        ),
        QuizQuestion(
            question = "What did Mordecai say would happen if Esther did not approach the king?",
            options = listOf(
                "He would go to the king himself",
                "The Jewish people would find deliverance another way, but Esther's family would perish",
                "He would fast and pray alone",
                "God would send another messenger"
            ),
            correctIndex = 1
        ),
        QuizQuestion(
            question = "How many days did Esther ask the Jewish people to fast?",
            options = listOf("1 day", "3 days", "7 days", "40 days"),
            correctIndex = 1
        ),
        QuizQuestion(
            question = "What did Esther ask Mordecai and the Jews to do before she went to the king?",
            options = listOf("Prepare gifts", "Leave the city", "Fast for her", "Write another letter"),
            correctIndex = 2
        ),
        QuizQuestion(
            question = "What did Esther say she would do if she perished?",
            options = listOf(
                "She said she would not go to the king",
                "She said 'if I perish, I perish' — and went anyway",
                "She asked God for a sign first",
                "She sent Mordecai in her place"
            ),
            correctIndex = 1
        )
    )
}
