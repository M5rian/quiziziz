package game

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import game.managers.ActiveGamesManager
import game.managers.PendingGameManager
import mention

data class PendingGame(
    val id: String,
    val owner: Snowflake,
    private val questionCount: Int
) {
    private val pendingPlayers = mutableListOf<Snowflake>()
    private val players = mutableListOf(owner)

    fun generateMessage(builder: MessageCreateBuilder) =
        builder.apply {
            embed {
                val pendingPlayersMention = pendingPlayers.joinToString { it.mention() }
                description = "⏰ Waiting for $pendingPlayersMention"
                description += "\n Invite members using the dropdown"

                field {
                    name = "❓ Questions";
                    value = questionCount.toString()
                    inline = true
                }
                field {
                    name = "👥 Players"
                    val playersMention = players.joinToString { it.mention() }
                    value = "${players.size} Member ($playersMention)"
                    inline = true
                }
            }

            actionRow {
                userSelect("userSelect:$id")
            }
            actionRow {
                interactionButton(ButtonStyle.Primary, "join:$id") { label = "🥊 Join" }
                interactionButton(ButtonStyle.Primary, "leave:$id") { label = "🚶‍♂️ Leave" }
                interactionButton(ButtonStyle.Success, "start:$id") { label = "🚀 Start" }
            }
        }

    fun invitePlayer(user: User): Boolean {
        if (user.id == owner) return false

        when (user.id) {
            in pendingPlayers -> pendingPlayers.remove(user.id)
            in players -> players.remove(user.id)
            else -> pendingPlayers.add(user.id)
        }
        return true
    }

    suspend fun joinPlayer(user: User, event: ButtonInteractionCreateEvent) {
        if (user.id !in pendingPlayers) {
            event.interaction.respondEphemeral {
                content =
                    "I'm sorry but you're not invited to this game.You can start your own game if you want to though"
            }
            return
        }

        pendingPlayers.remove(user.id)
        players.add(user.id)
        event.interaction.updatePublicMessage { generateMessage(this) }
    }

    suspend fun leavePlayer(user: User, event: ButtonInteractionCreateEvent) {
        if (user.id !in players) {
            event.interaction.respondEphemeral {
                content = "Nothing to leave for you - you're not part of that game"
            }
            return
        }

        players.remove(user.id)
        event.interaction.updatePublicMessage { generateMessage(this) }
    }

    suspend fun start(
        event: ButtonInteractionCreateEvent,
        pendingGames: PendingGameManager,
        activeGames: ActiveGamesManager
    ) {
        if (event.interaction.user.id != owner) {
            event.interaction.respondEphemeral { content = "We're all excited but only the owner can start the game" }
            return
        }

        pendingGames.remove(id)
        val game = activeGames.create(id, questionCount, owner, players)
        game.displayRound(event.interaction)
    }

}