import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.Socket

class Client(ip: String, port: Int) : EndPoint {
  private val socket = Socket(ip, port)
  private val reader = HttpMessageReader(BufferedInputStream(socket.getInputStream()))
  private val writer = HttpMessageWriter(BufferedOutputStream(socket.getOutputStream()))

  /*
    GET /chat HTTP/1.1
    Host: server.example.com
    Upgrade: websocket
    Connection: Upgrade
    Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
    Origin: http://example.com
    Sec-WebSocket-Protocol: chat, superchat
    Sec-WebSocket-Version: 13
  */
  fun connect() {
    writer.writeLine("GET /chat HTTP/1.1")
    writer.writeLine("")
//    writer.writeLine("Host: 127.0.0.1")
//    writer.writeLine("Upgrade: websocket")
//    writer.writeLine("Connection: Upgrade")
//    writer.writeLine("Origin: 127.0.0.1")
//    writer.writeLine("Sec-WebSocket-Key: " + "dGhlIHNhbXBsZSBub25jZQ==")
//    writer.writeLine("Sec-WebSocket-Protocol: " + "whatever")
//    writer.writeLine("Sec-WebSocket-Version: 13")
    writer.flush()

    var s = reader.readLine()
    while (s == null) {
      s = reader.readLine()
    }
  }

  fun send(message: String) {

  }

  override fun onOpen(session: Session) {

  }

  override fun onMessage(session: Session, message: String) {

  }

  override fun onMessage(session: Session, message: ByteArray, messageLen: Int) {

  }

  override fun onClose(session: Session, closeReasonCode: Int, closeReasonPhrase: String) {

  }

  override fun onError(session: Session, t: Throwable) {

  }
}