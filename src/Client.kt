import java.net.Socket
import java.nio.charset.StandardCharsets

fun main(args: Array<String>) {
    val socket = Socket("localhost", 10301)
    val input = socket.getOutputStream()
    input.write("Hello Server".toByteArray(StandardCharsets.UTF_8))
}