package ktlo.app

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ktlo.log.*

val selector: SelectorManager = ActorSelectorManager(Dispatchers.IO)

suspend fun main() = branch("echo-server") {
    try {
        val serverSocket = aSocket(selector).tcp().bind("localhost", 1337)
        Log.info { "started server on ${serverSocket.localAddress}" }
        var i = 0
        branch("client") {
            while (true) {
                ++i
                val client = serverSocket.accept()
                launch { connection(client) }
                validate(i < 10) { "oh no, i = $i" }
            }
        }
    } catch (e: Exception) {
        Log.fatal(e) { "unknown exception" }
    }
}

suspend fun connection(socket: Socket) = branch(socket.identify()) {
    Log.debug { "connected" }
    socket.use {
        try {
            val input = socket.openReadChannel()
            Log.trace { "got input stream" }
            val output = socket.openWriteChannel(true)
            Log.trace { "got output stream" }
            input.copyTo(output)
        } catch (e: Exception) {
            Log.error(e) { "client error" }
        } finally {
            Log.debug { "disconnected" }
        }
    }
}

fun Socket.identify(): String {
    return remoteAddress.toString().replace("/", "")
}
