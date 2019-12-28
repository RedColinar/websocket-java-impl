package http

import ws.WebSocketHandler
import ws.WebSocketServer
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class HttpServer(port: Int) {
  private val serverSocket = ServerSocket(port)
  private val registry: HandlerRegistry = HandlerRegistry()

  private lateinit var serverThread: Thread

  init {
    registry.register(ExactPathMatcher("/hello"), object : HttpHandler {
      override fun handleRequest(socket: Socket, request: HttpRequest, response: HttpResponse): Boolean {
        response.code = HTTP_OK
        response.reasonPhrase = "OK"
        response.body = createBody("hello to you", CONTENT_TYPE_PLAIN)
        return true
      }
    })
    registry.register(ExactPathMatcher("/chat"), WebSocketHandler(WebSocketServer()))
  }

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

            clientSocket.shutdownOutput()
            clientSocket.shutdownInput()
            clientSocket.close()
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
    val handler = registry.lookup(request.uri)
    if (handler == null) {
      response.code = HTTP_NOT_FOUND
      response.reasonPhrase = "Not found"
      response.body = createBody("No handler found\n", CONTENT_TYPE_PLAIN)
      return true
    }

    return try {
      handler.handleRequest(socket, request, response)
    } catch (e: RuntimeException) {
      response.code = HTTP_INTERNAL_SERVER_ERROR
      response.reasonPhrase = "Internal Server Error"
      val stack = StringWriter()
      val stackWriter = PrintWriter(stack)
      stackWriter.use {
        e.printStackTrace(it)
      }
      response.body = createBody(stack.toString(), CONTENT_TYPE_PLAIN)
      true
    }
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
