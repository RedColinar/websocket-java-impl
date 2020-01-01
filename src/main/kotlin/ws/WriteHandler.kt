package ws

import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream


class WriteHandler(outputStream: OutputStream) {
  val bufferedOutputStream = BufferedOutputStream(outputStream, 1024)

  fun write(frame: Frame, writeCallback: WriteCallback) {

  }
}

interface WriteCallback {
  fun onFailure(e: IOException?)
  fun onSuccess()
}
