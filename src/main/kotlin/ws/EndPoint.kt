package ws

interface EndPoint {

  fun onOpen(session: Session)

  fun onMessage(session: Session, message: String)

  fun onMessage(session: Session, message: ByteArray, messageLen: Int)

  fun onClose(session: Session, closeReasonCode: Int, closeReasonPhrase: String)

  fun onError(session: Session, t: Throwable)
}

abstract class SimpleEndPoint : EndPoint {
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