package ws

class WebSocketServer : SimpleEndPoint() {

  override fun onOpen(session: Session) {
    println("Server onOpen")
  }

  override fun onMessage(session: Session, message: String) {
    println("server 收到消息 $message")
    session.sendText("server 发送消息 hello")
  }

  override fun onMessage(session: Session, message: ByteArray, messageLen: Int) {

  }

  override fun onClose(session: Session, closeReasonCode: Int, closeReasonPhrase: String) {
    println("Server onClose")
  }

  override fun onError(session: Session, t: Throwable) {
    println("Server onError")
  }
}