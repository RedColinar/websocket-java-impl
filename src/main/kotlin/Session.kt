interface Session {

  fun sendText(payload: String)

  fun sendBinary(payload: ByteArray)

  fun close(closeReason: Int, reasonPhrase: String)

  fun isOpen(): Boolean
}