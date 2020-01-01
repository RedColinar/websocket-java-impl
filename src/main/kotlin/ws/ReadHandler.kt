package ws

import java.io.BufferedInputStream
import java.io.InputStream

class ReadHandler(inputStream: InputStream, val endpoint: SimpleEndPoint) {

  val bufferedInput: BufferedInputStream = BufferedInputStream(inputStream, 1024)

  fun readLoop(readCallback: ReadCallback) {
    val frame = Frame()

  }
}

interface ReadCallback {
  fun onCompleteFrame(opcode: Byte, payload: ByteArray, payloadLen: Int)
}
