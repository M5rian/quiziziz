import dev.kord.common.entity.TextInputStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.core.on
import game.managers.ActiveGamesManager
import game.managers.IdManager
import game.managers.PendingGameManager
import quiz.Quiz

val idManager = IdManager()
val pendingGames = PendingGameManager(idManager)
val activeGames = ActiveGamesManager(idManager)
val quizQuestions = listOf(
    Quiz("animals").load(),
    Quiz("entertainment").load(),
    Quiz("general").load(),
    Quiz("geography").load(),
    Quiz("movies").load(),
    Quiz("science-technology").load(),
    Quiz("sports").load(),
)

suspend fun main() {
    val token = "OTI2MDc4MjU1MDMzODg4ODE5.GroMia.OW5Y_xcyaGntEUvXvXv3HMBZdYSxj4CyDGWrQs"
    val kord = Kord(token)

    kord.on<ChatInputCommandInteractionCreateEvent> { createQuizRequest(this) }
    kord.on<ModalSubmitInteractionCreateEvent> { createQuizResponse(this) }
    kord.on<SelectMenuInteractionCreateEvent> {
        val componentId = interaction.componentId
        val gameId = componentId.split(":").last()
        val game = pendingGames.get(gameId) ?: run {
            interaction.respondEphemeral { content = "Invalid game" }
            return@on
        }
        if (interaction.user.id != game.owner) {
            interaction.respondEphemeral { content = "You're not the owner of the game" }
            return@on
        }

        val user = this.interaction.resolvedObjects?.users?.values?.firstOrNull() ?: run {
            interaction.respondEphemeral { content = "You didn't select a member" }
            return@on
        }

        val added = game.invitePlayer(user)
        if (added) interaction.updatePublicMessage { game.generateMessage(this) }
        else interaction.respondEphemeral { content = "You can't remove yourself." }
    }
    kord.on<ButtonInteractionCreateEvent> {
        val user = interaction.user
        val (action, gameId) = interaction.componentId.split(":")
        when (action) {
            "join" -> pendingGames.get(gameId)?.joinPlayer(user, this) ?: interaction.gameNotFound()
            "leave" -> pendingGames.get(gameId)?.leavePlayer(user, this) ?: interaction.gameNotFound()
            "start" -> pendingGames.get(gameId)?.start(this, pendingGames, activeGames) ?: interaction.gameNotFound()
            "guess" -> activeGames.get(gameId)?.guess(interaction)
        }
    }

    kord.createGlobalChatInputCommand("quiz", "Start a quiz!")
    println("Starting bot!")
    kord.login()
}

suspend fun createQuizRequest(event: ChatInputCommandInteractionCreateEvent) {
    if (pendingGames.all().any { it.owner == event.interaction.user.id } || activeGames.all()
            .any { it.owner == event.interaction.user.id }) {
        event.interaction.respondEphemeral { content = "Closed your previous quiz" }
        val gameId = pendingGames.all().find { it.owner == event.interaction.user.id }?.id
            ?: activeGames.all().find { it.owner == event.interaction.user.id }?.id ?: return
        pendingGames.remove(gameId)
        activeGames.remove(gameId)
    }

    event.interaction.modal("Start a quiz!", "quiz:settings") {
        actionRow {
            textInput(TextInputStyle.Short, "questions-count", "Questions count")
        }
    }
}

suspend fun createQuizResponse(event: ModalSubmitInteractionCreateEvent) {
    val textInputs = event.interaction.actionRows.firstOrNull()?.textInputs?.values ?: run {
        event.interaction.respondEphemeral { content = "Failed to read modal" }
        return
    }
    val questionCount = textInputs.firstOrNull()?.value?.toIntOrNull() ?: run {
        event.interaction.respondEphemeral { content = "Failed to parse question count" }
        return
    }

    val ownerId = event.interaction.user.id
    val game = pendingGames.create(ownerId, questionCount)
    event.interaction.respondPublic { game.generateMessage(this) }
}
