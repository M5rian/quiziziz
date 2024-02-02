package game.managers

import java.util.*

abstract class GenericGameManager<T>(val idManager: IdManager) {
    private val map = mutableMapOf<String, T>()

    fun insert(id: String, game: T) {
        map[id] = game
    }

    fun get(id: String) = map[id]

    fun remove(id:String) = map.remove(id)

    fun all() = map.values

}