import http.HttpMessageReader
import http.HttpMessageWriter
import http.HttpServer
import ws.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.URL

class TestClient {

  fun newWebSocket(path: String, endPoint: EndPoint): WebSocket {
    val url = URL(path)
    val host = url.host
    val ip = InetAddress.getByName(host).hostAddress
    val port = url.port
    val urlPath = url.path
    val socket = Socket(ip, port)
    val reader = HttpMessageReader(BufferedInputStream(socket.getInputStream()))
    val writer = HttpMessageWriter(BufferedOutputStream(socket.getOutputStream()))

    writer.writeLine("GET $urlPath HTTP/1.1")
    writer.writeLine("Host: $host")
    writer.writeLine("Upgrade: websocket")
    writer.writeLine("Connection: Upgrade")
    writer.writeLine("Origin: $host")
    writer.writeLine("Sec-WebSocket-Key: " + "dGhlIHNhbXBsZSBub25jZQ==")
    writer.writeLine("Sec-WebSocket-Protocol: " + "whatever")
    writer.writeLine("Sec-WebSocket-Version: 13")
    writer.writeLine()
    writer.flush()

    var s = reader.readLine()
    while (s != null) {
      s = reader.readLine()
    }

    val factory: WebSocket.Factory = RealWebSocketFactory()
    return factory.create(endPoint)
  }

  fun requestHttp(path: String) {
    val url = URL(path)
    val host = url.host
    val ip = InetAddress.getByName(host).hostAddress
    val port = url.port
    val urlPath = url.path
    val socket = Socket(ip, port)
    val reader = HttpMessageReader(BufferedInputStream(socket.getInputStream()))
    val writer = HttpMessageWriter(BufferedOutputStream(socket.getOutputStream()))

    writer.writeLine("GET $urlPath HTTP/1.1")
    writer.writeLine("Host: $host")
    writer.writeLine("")
    writer.flush()

    var s = reader.readLine()
    while (s != null) {
      println(s)
      s = reader.readLine()
    }
    socket.shutdownInput()
    socket.shutdownOutput()
    socket.close()
  }

}