package game.managers

import dev.kord.common.entity.Snowflake
import java.util.*

class IdManager {
    private val inUse = mutableListOf<String>()

    fun generateId(): String {
        var id = UUID.randomUUID().toString()
        while (id in inUse) {
            id = UUID.randomUUID().toString()
        }
        inUse.add(id)
        return id
    }

    fun remove(id:String) {
        inUse.remove(id)
    }

}