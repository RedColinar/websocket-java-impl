package http

import java.io.BufferedOutputStream

class HttpMessageWriter(val output: BufferedOutputStream) {
  private val CRLF = "\r\n".toByteArray()

  fun writeLine(line: String) {
    var i = 0
    val length = line.length
    while (i < length) {
      val c = line[i]
      output.write(c.toInt())
      i++
    }
    output.write(CRLF)
  }

  fun writeLine() {
    output.write(CRLF)
  }

  fun flush() {
    output.flush()
  }
}