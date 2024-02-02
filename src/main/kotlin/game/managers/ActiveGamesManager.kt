package game.managers

import dev.kord.common.entity.Snowflake
import game.Game

class ActiveGamesManager(idManager: IdManager) : GenericGameManager<Game>(idManager) {

    fun create(id: String, questionCount: Int, owner: Snowflake, players: List<Snowflake>): Game {
        val game = Game(id, questionCount, owner, players)
        insert(id, game)
        return game
    }

}