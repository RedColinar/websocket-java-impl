package ws

import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception

class WriteHandler(outputStream: OutputStream) {
  private val bufferedOutputStream = BufferedOutputStream(outputStream, 1024)

  @Synchronized
  fun write(frame: Frame, writeCallback: WriteCallback) {
    try {
      frame.writeTo(bufferedOutputStream)
      bufferedOutputStream.flush()
      writeCallback.onSuccess()
    } catch (e: IOException) {
      writeCallback.onFailure(e)
    }
  }
}

interface WriteCallback {
  fun onFailure(e: IOException)
  fun onSuccess()
}
