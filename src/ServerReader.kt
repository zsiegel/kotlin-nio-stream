import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

class ServerReader : Runnable {

    companion object {
        private const val BUFFER_SIZE = 8
    }

    val selector: Selector = Selector.open()
    private val clientBuffers: MutableMap<String, ByteBuffer> = mutableMapOf()

    override fun run() {

        println("[${Thread.currentThread().name}] - ServerAcceptor reader thread is now running")
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

        println("[${Thread.currentThread().name}] - ServerAcceptor reader thread stopping")
    }

    private fun read(selectionKey: SelectionKey) {

        val clientId = selectionKey.attachment() as String
        val socket = selectionKey.channel() as SocketChannel

        val tempBuffer = ByteBuffer.allocate(BUFFER_SIZE)
        val bytesRead = socket.read(tempBuffer)
        if (bytesRead > 0) {

            println("[${Thread.currentThread().name}] - $bytesRead bytes from $clientId")

            var endFound = false
            var clientBuffer = clientBuffers[clientId]
            if (clientBuffer == null) {

                //TODO make buffer only the length it needs to be is message is smaller than BUFFER_SIZE

                clientBuffer = ByteBuffer.allocate(BUFFER_SIZE * 2)
                clientBuffers[clientId] = clientBuffer
            }

            for (byte in tempBuffer.array().slice(0 until bytesRead)) {

                if (byte.toChar() == '\n') {
                    endFound = true
                    break
                }

                if (clientBuffer?.position() == clientBuffer?.capacity()) {
                    val newBuffer = ByteBuffer.allocate(clientBuffer?.capacity()!! * 2)
                    newBuffer.put(clientBuffer.array())
                    clientBuffers[clientId] = newBuffer
                    clientBuffer = newBuffer
                }
                clientBuffer?.put(byte)
            }

            if (endFound) {
                val buffer = clientBuffers[clientId]
                val messageBuffer = buffer!!.array().slice(0 until buffer.position()).toByteArray()
                val message = String(messageBuffer, StandardCharsets.UTF_8)
                println("[${Thread.currentThread().name}] - $clientId - PAYLOAD { $message }")
                clientBuffers.remove(clientId)
            }
        }
    }
}