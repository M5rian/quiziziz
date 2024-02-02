package quiz

data class Question(
    val question: String,
    val answer: Int,
    val options: List<String>
)