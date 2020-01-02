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

  private val readHandler = ReadHandler(rawInput, endpoint)
  private val writeHandler = WriteHandler(rawOutput)

  private val isOpen: AtomicBoolean = AtomicBoolean(false)
  @Volatile
  private var sentClose = false

  override fun sendText(payload: String) {
    doWrite(createTextFrame(payload))
  }

  override fun sendBinary(payload: ByteArray) {
    doWrite(createBinaryFrame(payload))
  }

  override fun close(closeReason: Int, reasonPhrase: String) {
    sendClose(closeReason, reasonPhrase)
    markSignalClose(closeReason, reasonPhrase)
  }

  override fun isOpen(): Boolean = isOpen.get()

  fun handle() {
    markSignalOpen()
    try {
      readHandler.readLoop(readCallback)
    } catch (e: EOFException) {
      markSignalClose(UNEXPECTED_CONDITION, "EOF while reading")
    } catch (e: IOException) {
      markSignalClose(CLOSED_ABNORMALLY, "")
      throw e
    }
  }

  private val readCallback = object : ReadCallback {
    override fun onCompleteFrame(opcode: Byte, payload: ByteArray, payloadLen: Int) {
      when (opcode) {
        Frame.OPCODE_CONNECTION_CLOSE -> handleClose(payload, payloadLen)
        Frame.OPCODE_CONNECTION_PING -> handlePing(payload, payloadLen)
        Frame.OPCODE_CONNECTION_PONG -> handlePong(payload, payloadLen)
        Frame.OPCODE_TEXT_FRAME -> handleTextFrame(payload, payloadLen)
        Frame.OPCODE_BINARY_FRAME -> handleBinaryFrame(payload, payloadLen)
        else -> signalError(IOException("Unsupport frame opcode = $opcode"))
      }
    }
  }

  private fun doWrite(frame: Frame) {
    if (signalErrorIfNotOpen()) return
    writeHandler.write(frame, errorWriteCallback)
  }

  private fun handleClose(payload: ByteArray, payloadLen: Int) {
    val closeCode: Int
    val closeReason: String

    if (payloadLen >= 2) {
      val part1 = payload[0].toInt() and 0xff shl 8
      val part2 = payload[1].toInt() and 0xff
      closeCode = part1 or part2
      closeReason = if (payloadLen > 2) String(payload, 2, payloadLen - 2) else ""
    } else {
      closeCode = CLOSED_ABNORMALLY
      closeReason = "Unparseable close frame"
    }

    if (!sentClose) {
      sendClose(NORMAL_CLOSURE, "received close frame")
    }
    markSignalClose(closeCode, closeReason)
  }

  private fun sendClose(closeCode: Int, closeReason: String) {
    doWrite(createCloseFrame(closeCode, closeReason))
    markSignalClose(closeCode, closeReason)
  }

  private fun handlePing(payload: ByteArray, payloadLen: Int) {
    doWrite(createPongFrame(payload, payloadLen))
  }

  private fun handlePong(payload: ByteArray, payloadLen: Int) {
    // do nothing ..
  }

  private fun handleTextFrame(payload: ByteArray, payloadLen: Int) {
    endpoint.onMessage(this, String(payload, 0, payloadLen))
  }

  private fun handleBinaryFrame(payload: ByteArray, payloadLen: Int) {
    endpoint.onMessage(this, payload, payloadLen)
  }

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

  private fun signalError(e: IOException) {
    endpoint.onError(this, e)
  }

  private fun signalErrorIfNotOpen(): Boolean {
    if (!isOpen()) {
      signalError(IOException("Session is closed"))
      return true
    }
    return false
  }

  private val errorWriteCallback = object : WriteCallback {
    override fun onFailure(e: IOException) = signalError(e)
    override fun onSuccess() {}
  }
}