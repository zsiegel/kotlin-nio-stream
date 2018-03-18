import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

class ServerReader : Runnable {

    val selector: Selector = Selector.open()

    override fun run() {

        println("[${Thread.currentThread().name}] - Server reader thread is now running")
        while (!Thread.interrupted()) {

            selector.select(1000)

            val iter = selector.selectedKeys().iterator()
            iter.forEach { key ->

                if (key.isReadable) {
                    read(key)
                }

                iter.remove()
            }
        }

        println("[${Thread.currentThread().name}] - Server reader thread stopping")
    }

    private fun read(selectionKey: SelectionKey) {
        val clientId = selectionKey.attachment() as String

        val socket = selectionKey.channel() as SocketChannel

        //We know this is more than what we are sending - we will deal with multiple buffers later
        val buffer = ByteBuffer.allocate(512)

        val bytesRead = socket.read(buffer)
        if (bytesRead > 0) {
            println("---- [${Thread.currentThread().name}] - Reading from $clientId")

            println("[${Thread.currentThread().name}] - $bytesRead bytes from $clientId")

            val message = String(buffer.array().slice(0 until bytesRead).toByteArray(), StandardCharsets.UTF_8)
            println("[${Thread.currentThread().name}] - PAYLOAD { $message }")
        }
    }
}