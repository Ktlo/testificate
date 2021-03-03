package ktlo.app

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ktlo.log.*
import kotlin.concurrent.thread

val selector: SelectorManager = ActorSelectorManager(Dispatchers.IO)

/*
   Простой эхо TCP сервер. Хорош тем, что на нём можно сразу показать сессии,
   учавствующие в домене журнала.
 */
suspend fun main() = branch("echo-server") {
    run {
        // В этом run блоке демонстрируется передача объекта журналирования в другой поток.
        // В данном случае это поток, обрабатывающий выход из программы.
        val log = Log.current
        val hook = thread(start = false) {
            Log.using(log) {
                Log.info { "application stopped" }
            }
        }
        Runtime.getRuntime().addShutdownHook(hook)
    }
    try {
        val serverSocket = aSocket(selector).tcp().bind("localhost", 1337)
        // Сообщение выстраивается с помощью стандартных языковых конструкций Kotlin
        Log.info { "started server on ${serverSocket.localAddress}" }
        var i = 0

        // Поддерево домена, где работают клиентские сессии сервера
        branch("client", Dispatchers.Default) {
            while (true) {
                ++i
                val client = serverSocket.accept()
                launch { connection(client) }

                // Максимально исскуственный пример нарушения какого-то условия,
                // которое приводит программу к неправильному состоянию.
                // В этом месте логгирование по сути совмещено с ассертами.
                validate(i < 5) { "oh no, i = $i" }
            }
        }
    } catch (e: Exception) {
        Log.fatal(e) { "unknown exception" }
    }
}

// Подпросесс, работающий с одним клиентом. Первым делом он формирует новый
// уровень домена журнала, соответствующий клиенту. Отдалённо это напоминает
// сессионное журналирование, так как можно по домену определить сессию.
suspend fun connection(socket: Socket) = branch(socket.identify()) {
    // Для примера применим другой уровень важности сообщения
    Log.debug { "connected" }
    socket.use {
        // В этом месте мы вызываем функцию некого объекта, который не привязан к сессии.
        // По сути тут демонстрируется, как можно применить такой подход к именованию доменов
        // в случае подобных объектов, к которым идёт обращение из разных точек программы.
        ConnectionManager.enter(socket)
        try {
            val input = socket.openReadChannel()
            // Ещё один уровень журналирования
            Log.trace { "got input stream" }
            val output = socket.openWriteChannel(true)
            Log.trace { "got output stream" }
            // Просто так ещё один уровень домена, почему бы и да
            branch("next") {
                Log.warning { "additional level" }
            }
            // Ключевая часть эхо сервера
            input.copyTo(output)
        } catch (e: Exception) {
            // Пример вывода исключения с комментарием
            Log.error(e) { "client error" }
        } finally {
            ConnectionManager.leave(socket)
            Log.debug { "disconnected" }
        }
    }
}

fun Socket.identify(): String {
    return '#' + remoteAddress.toString().replace("/", "")
}
