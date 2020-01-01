package ws

import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

class WebSocketSession(
  rawInput: InputStream,
  rawOutput: OutputStream,
  private val endpoint: SimpleEndPoint
) : Session {

  private val isOpen: AtomicBoolean = AtomicBoolean(false)
  private val readHandler = ReadHandler(rawInput, endpoint)
  private val writeHandler = WriteHandler(rawOutput)

  fun handle() {
    markSignalOpen()
    try {
      readHandler.readLoop(readCallback)
    } catch (e: EOFException) {
      markSignalClose(UNEXPECTED_CONDITION, "EOF while reading")
    } catch (e: IOException) {
      markSignalClose(CLOSED_ABNORMALLY, "")
    }
  }

  private val readCallback = object : ReadCallback {
    override fun onCompleteFrame(opcode: Byte, payload: ByteArray, payloadLen: Int) {

    }
  }

  override fun sendText(payload: String) {

  }

  override fun sendBinary(payload: ByteArray) {

  }

  override fun close(closeReason: Int, reasonPhrase: String) {

  }

  override fun isOpen(): Boolean = isOpen.get()

  private fun markSignalOpen() {
    if (!isOpen.get()) {
      isOpen.set(true)
      endpoint.onOpen(this)
    }
  }

  private fun markSignalClose(closeCode: Int, reasonPhrase: String) {
    if (isOpen.get()) {
      isOpen.set(false)
      endpoint.onClose(this, closeCode, reasonPhrase)
    }
  }
}