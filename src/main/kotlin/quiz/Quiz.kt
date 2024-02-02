package quiz

class Quiz(val category: String) {

    fun load(): List<Question> {
        val path = "OpenTriviaQA/categories/$category"
        val text = this.javaClass.classLoader.getResource(path)?.readText()
            ?: throw Exception("Couldn't find quiz file: $path")
        val questions = text
            .split("\n\n")
            .filter { it.isNotBlank() }
            .map { it.split("\n") }
            .mapNotNull {
                val question = it[0]
                if (it.size != 6) return@mapNotNull null
                val correctAnswer = it[1].removePrefix("^ ")
                val options = it.slice(2 until it.size)

                val correctAnswerIndex = options.map { option -> option.substring(2) }.indexOf(correctAnswer)
                Question(question, correctAnswerIndex, options)
            }
        return questions
    }

}