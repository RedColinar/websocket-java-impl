interface EndPoint {

  fun onOpen(session: Session)

  fun onMessage(session: Session, message: String)

  fun onMessage(session: Session, message: ByteArray, messageLen: Int)

  fun onClose(session: Session, closeReasonCode: Int, closeReasonPhrase: String)

  fun onError(session: Session, t: Throwable)
}