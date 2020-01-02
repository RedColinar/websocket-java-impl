package ws

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ReadHandler(inputStream: InputStream, val endpoint: SimpleEndPoint) {

  private val bufferedInput: BufferedInputStream = BufferedInputStream(inputStream, 1024)
  private val currentPayload = ByteArrayOutputStream()

  fun readLoop(readCallback: ReadCallback) {
    val frame = Frame()
    do {
      frame.readFrom(bufferedInput)
      currentPayload.write(frame.payloadData, 0, frame.payloadLen.toInt())
      if (frame.fin) {
        val completePayload = currentPayload.toByteArray()
        readCallback.onCompleteFrame(frame.opcode, completePayload, completePayload.size)
        currentPayload.reset()
      }
    } while (frame.opcode != Frame.OPCODE_CONNECTION_CLOSE)
  }
}

interface ReadCallback {
  fun onCompleteFrame(opcode: Byte, payload: ByteArray, payloadLen: Int)
}
