package ktlo.app

import io.ktor.network.sockets.*
import ktlo.log.Log
import ktlo.log.subprogram

/*
   Некий сторонний объект, к которому идёт обращение из разных частей программы
 */
object ConnectionManager {
    private val sockets = mutableSetOf<Socket>()

    fun enter(socket: Socket) = subprogram("@connection-manager") {
        sockets += socket
        Log.info { "added socket" }
    }

    fun leave(socket: Socket) = subprogram("@connection-manager") {
        sockets -= socket
        Log.info { "removed socket" }
    }
}
