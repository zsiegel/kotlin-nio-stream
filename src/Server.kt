import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel

fun main(args: Array<String>) {
    val server = Server(10301)

    val serverThread = Thread(server)
    Runtime.getRuntime().addShutdownHook(Thread {
        serverThread.interrupt()
    })

    serverThread.start()
}

class Server(port: Int) : Runnable {

    val socketChannel: ServerSocketChannel = ServerSocketChannel.open()
    val selector: Selector

    init {
        socketChannel.socket().bind(InetSocketAddress(port))
        socketChannel.configureBlocking(false)

        selector = Selector.open()
        socketChannel.register(selector, SelectionKey.OP_ACCEPT)
    }

    override fun run() {

        println("Server socket listening on ${socketChannel.socket().localPort}")

        try {

            while (socketChannel.isOpen && !Thread.interrupted()) {

                selector.select(1000)

                val iter = selector.selectedKeys().iterator()
                iter.forEach { key ->

                    if (key.isAcceptable) {
                        accept(key)
                    }

                    if (key.isReadable) {
                        val clientId = key.attachment() as String

                    }

                    iter.remove()
                }
            }

            println("Socket closing gracefully")
            socketChannel.close()

        } catch (e: IOException) {
            System.err.println("Closing down socket - ${e.printStackTrace()}")
        }
    }

    private fun accept(selectionKey: SelectionKey) {
        val serverSocket = selectionKey.channel() as ServerSocketChannel

        val clientSocket = serverSocket.accept()
        clientSocket.configureBlocking(false)

        val address = clientSocket.socket().inetAddress.toString()
        val port = clientSocket.socket().localPort
        val clientId = "$address:$port"

        clientSocket.register(selector, SelectionKey.OP_READ, clientId)
    }
}