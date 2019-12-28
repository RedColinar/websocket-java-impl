package http

class HandlerRegistry {
  private val pathMatchers = ArrayList<PathMatcher>()
  private val httpHandlers = ArrayList<HttpHandler>()

  fun register(path: PathMatcher, handler: HttpHandler) {
    pathMatchers.add(path)
    httpHandlers.add(handler)
  }

  fun unregister(path: PathMatcher, handler: HttpHandler): Boolean {
    val index = pathMatchers.indexOf(path)
    if (index >= 0) {
      if (handler === httpHandlers[index]) {
        pathMatchers.removeAt(index)
        httpHandlers.removeAt(index)
        return true
      }
    }
    return false
  }

  fun lookup(path: String): HttpHandler? {
    for (i in 0 until pathMatchers.size) {
      if (pathMatchers[i].match(path)) {
        return httpHandlers[i]
      }
    }
    return null
  }
}