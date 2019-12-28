package http

import java.util.regex.Pattern

interface PathMatcher {
  fun match(path: String): Boolean
}

class ExactPathMatcher(private val path: String) : PathMatcher {
  override fun match(path: String): Boolean = this.path == path
}

class RegexpPathMatcher(private val pattern: Pattern) : PathMatcher {
  override fun match(path: String): Boolean = pattern.matcher(path).matches()
}