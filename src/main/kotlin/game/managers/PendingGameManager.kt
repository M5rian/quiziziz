package game.managers

import dev.kord.common.entity.Snowflake
import game.PendingGame

class PendingGameManager(idManager: IdManager) : GenericGameManager<PendingGame>(idManager) {

    fun create(owner: Snowflake, questionCount: Int): PendingGame {
        val id = idManager.generateId()
        val game = PendingGame(id, owner, questionCount)
        insert(id, game)
        println("inserted $id")
        return game
    }

}