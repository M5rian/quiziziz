package game

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.delay
import mention
import quizQuestions

data class Game(
    val id: String,
    val rounds: Int,
    val owner: Snowflake,
    val players: List<Snowflake>
) {
    var question = quizQuestions.random().random()
    var round = 0
    val playerGuesses = mutableMapOf<Snowflake, Int>()
    val correctGuesses = mutableMapOf<Snowflake, Int>()

    init {
        players.forEach {
            correctGuesses[it] = 0
        }
    }

    /**
     * @param silent Whether to acknowledge the interaction or not
     */
    suspend fun displayRound(interaction: ButtonInteraction, silent: Boolean = false) {
        val embedBuilder: EmbedBuilder.() -> Unit = {
            title = "Round ${round + 1}!"
            description = question.question
            question.options.forEachIndexed { index, option ->
                field {
                    name = option
                    if (playerGuesses.size == players.size) {
                        if (question.answer == index) name += " ✅"

                        val mentions = playerGuesses.filter { it.value == index }.map { it.key.mention() }
                        value = mentions.joinToString()
                    }
                }
            }
            footer { text = "${playerGuesses.size}/${players.size}" }
        }

        val actionRow: ActionRowBuilder.() -> Unit = {
            interactionButton(ButtonStyle.Secondary, "guess:$id:a") { label = "A" }
            interactionButton(ButtonStyle.Secondary, "guess:$id:b") { label = "B" }
            interactionButton(ButtonStyle.Secondary, "guess:$id:c") { label = "C" }
            interactionButton(ButtonStyle.Secondary, "guess:$id:d") { label = "D" }
        }

        if (silent) interaction.message.edit {
            embed(embedBuilder)
            actionRow(actionRow)
        } else interaction.updatePublicMessage {
            embed(embedBuilder)
            actionRow(actionRow)
        }
    }

    suspend fun guess(interaction: ButtonInteraction) {
        println("hi")
        if (interaction.user.id in playerGuesses.keys) {
            interaction.respondEphemeral { content = "You've already took your guess." }
            return
        }

        val (_, _, guess) = interaction.componentId.split(":")
        val guessIndex = when (guess) {
            "a" -> 0
            "b" -> 1
            "c" -> 2
            "d" -> 3
            else -> {
                interaction.respondEphemeral { content = "This option doesn't exist" }
                return
            }
        }
        playerGuesses[interaction.user.id] = guessIndex
        displayRound(interaction)

        if (playerGuesses.size == players.size) {
            playerGuesses.filter { it.value == question.answer }.forEach { (userId, _) ->
                val userCorrectGuesses = correctGuesses.getOrPut(userId) { 0 }
                correctGuesses[userId] = userCorrectGuesses + 1
            }
            delay(2000)

            if (round + 1 == rounds) {
                endGame(interaction)
            } else {
                println("new round")
                round++
                question = quizQuestions.random().random()
                playerGuesses.clear()
                displayRound(interaction, true)
            }

        }
    }

    private suspend fun endGame(interaction: ButtonInteraction) {
        val leaderboard = correctGuesses.toList().sortedBy { it.second }.asReversed()
        println(leaderboard)
        interaction.message.edit {
            embed {
                title = "\uD83C\uDF89 End!"
                description = leaderboard
                    .mapIndexed { index, entry -> "${index + 1}. ${entry.first.mention()} - ${entry.second} correct" }
                    .joinToString("\n")
            }
        }
    }

}