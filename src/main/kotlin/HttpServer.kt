import java.io.*
import java.net.ServerSocket
import java.net.Socket

class HttpServer(port: Int) {
  private val serverSocket = ServerSocket(port)
  private lateinit var serverThread: Thread

  fun start() {
    serverThread = Thread {
      val request = HttpRequest()
      val response = HttpResponse()

      outer@ while (true) {
        if (Thread.currentThread().isInterrupted) break
        try {
          val clientSocket = serverSocket.accept()
          val input = clientSocket.getInputStream()
          val output = clientSocket.getOutputStream()
          val reader = HttpMessageReader(BufferedInputStream(input))
          val writer = HttpMessageWriter(BufferedOutputStream(output))

          inner@ while (readRequest(request, reader)) {
            response.reset()
            val keepGoing = dispatch(clientSocket, request, response)
            if (!keepGoing) break@inner
            writeFullResponse(response, writer)
          }
        } catch (e: Exception) {
          e.printStackTrace()
          break@outer
        }
      }
    }
    // serverThread.isDaemon = true
    serverThread.start()
  }

  private fun dispatch(socket: Socket, request: HttpRequest, response: HttpResponse): Boolean {
    println(request.toString())
    return false
  }

  fun stop() {
    serverThread.interrupt()
  }

  companion object {
    private fun readRequest(request: HttpRequest, reader: HttpMessageReader): Boolean {
      request.reset()
      val requestLine = reader.readLine()
      requestLine ?: return false
      val parts = requestLine.split(Regex(" "), 3)
      if (parts.size != 3) throw IOException("Invalid request line: $requestLine")

      request.method = parts[0]
      request.uri = parts[1]
      request.protocol = parts[2]

      readHeader(request, reader)
      return true
    }

    private fun readHeader(message: HttpMessage, reader: HttpMessageReader) {
      while (true) {
        val headerLine = reader.readLine()
        headerLine ?: throw EOFException()
        if ("" == headerLine) break
        val parts = headerLine.split(Regex(":"), 2)
        if (parts.size != 2) throw IOException("Malformed header: $headerLine")
        message.headerNames.add(parts[0])
        message.headerValues.add(parts[1])
      }
    }

    private fun writeHeader(response: HttpResponse, writer: HttpMessageWriter) {
      for (i in 0 until response.headerNames.size) {
        val name = response.headerNames[i]
        val value = response.headerValues[i]
        writer.writeLine("$name: $value")
      }
    }

    private fun writeFullResponse(response: HttpResponse, writer: HttpMessageWriter) {
      response.prepare()
      writeResponseMessage(response, writer)
      if (response.body != null) {
        response.body!!.writeTo(writer.output)
      }
    }

    fun writeResponseMessage(response: HttpResponse, writer: HttpMessageWriter) {
      writer.writeLine("HTTP/1.1 ${response.code} ${response.reasonPhrase}")
      writeHeader(response, writer)
      writer.writeLine()
      writer.flush()
    }
  }
}

class HttpMessageReader(val input: BufferedInputStream) {
  private val buffer = StringBuilder()
  private val newLineDetector = NewLineDetector()

  fun readLine(): String? {
    while (true) {
      input.available()
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
      val STATE_ON_OTHER = 1
      val STATE_ON_CR = 2
      val STATE_ON_CRLF = 3
    }
  }
}

class HttpMessageWriter(val output: BufferedOutputStream) {
  private val CRLF = "\r\n".toByteArray()

  fun writeLine(line: String) {
    var i = 0
    val N = line.length
    while (i < N) {
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
