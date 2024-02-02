import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.entity.interaction.SelectMenuInteraction

fun Snowflake.mention() = "<@$value>"

suspend fun ActionInteraction.gameNotFound() {
    respondEphemeral {
        content = "Couldn't find your game"
    }
}