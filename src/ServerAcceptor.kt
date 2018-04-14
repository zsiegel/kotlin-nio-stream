import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel

fun main(args: Array<String>) {
    val server = ServerAcceptor(10301)

    val serverThread = Thread(server)
    serverThread.name = "NIO ServerAcceptor Acceptor"
    Runtime.getRuntime().addShutdownHook(Thread {
        serverThread.interrupt()
    })

    serverThread.start()
}

class ServerAcceptor(port: Int) : Runnable {

    val socketChannel: ServerSocketChannel = ServerSocketChannel.open()
    val selector: Selector

    val readerThread: Thread
    val serverReader: ServerReader = ServerReader()

    init {
        socketChannel.socket().bind(InetSocketAddress(port))
        socketChannel.configureBlocking(false)

        selector = Selector.open()
        socketChannel.register(selector, SelectionKey.OP_ACCEPT)

        readerThread = Thread(serverReader)
        readerThread.name = "NIO ServerAcceptor Reader"
        readerThread.start()
    }

    override fun run() {

        println("[${Thread.currentThread().name}] - ServerAcceptor socket listening on ${socketChannel.socket().localPort}")

        try {

            while (socketChannel.isOpen && !Thread.interrupted()) {

                selector.select(1000)

                val iter = selector.selectedKeys().iterator()
                iter.forEach { key ->

                    if (key.isAcceptable) {
                        accept(key)
                    }

                    iter.remove()
                }
            }

            println("[${Thread.currentThread().name}] - Socket closing gracefully")
            socketChannel.close()

        } catch (e: IOException) {
            System.err.println("[${Thread.currentThread().name}] - Closing down socket - ${e.printStackTrace()}")
        }

        readerThread.interrupt()
    }

    private fun accept(selectionKey: SelectionKey) {
        val serverSocket = selectionKey.channel() as ServerSocketChannel

        val clientSocket = serverSocket.accept()
        clientSocket.configureBlocking(false)

        val address = clientSocket.socket().inetAddress.toString()
        val port = clientSocket.socket().port
        val clientId = "$address:$port"

        clientSocket.register(serverReader.selector, SelectionKey.OP_READ, clientId)
    }
}