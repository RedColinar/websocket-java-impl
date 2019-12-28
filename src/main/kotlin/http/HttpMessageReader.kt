package http

import java.io.BufferedInputStream

class HttpMessageReader(val input: BufferedInputStream) {
  private val buffer = StringBuilder()
  private val newLineDetector = NewLineDetector()

  fun readLine(): String? {
    while (true) {
      // 当无字节可读时，会阻塞
      val b = input.read()
      if (b < 0) return null

      val c = b.toChar()
      newLineDetector.accept(c)

      when (newLineDetector.state()) {
        NewLineDetector.STATE_ON_CRLF -> {
          val result = buffer.toString()
          buffer.setLength(0)
          return result
        }
        NewLineDetector.STATE_ON_CR -> {
        }
        NewLineDetector.STATE_ON_OTHER -> buffer.append(c)
      }
    }
  }

  private class NewLineDetector {

    private var state = STATE_ON_OTHER

    fun accept(c: Char) {
      when (state) {
        STATE_ON_OTHER -> if (c == '\r') {
          state = STATE_ON_CR
        }
        STATE_ON_CR -> if (c == '\n') {
          state = STATE_ON_CRLF
        } else {
          state = STATE_ON_OTHER
        }
        STATE_ON_CRLF -> if (c == '\r') {
          state = STATE_ON_CR
        } else {
          state = STATE_ON_OTHER
        }
        else -> throw IllegalArgumentException("Unknown state: $state")
      }
    }

    fun state(): Int {
      return state
    }

    companion object {
      const val STATE_ON_OTHER = 1
      const val STATE_ON_CR = 2
      const val STATE_ON_CRLF = 3
    }
  }
}