package ws

import java.io.*
import kotlin.experimental.or

class Frame {
  companion object {
    const val OPCODE_TEXT_FRAME: Byte = 0x1
    const val OPCODE_BINARY_FRAME: Byte = 0x2
    const val OPCODE_CONNECTION_CLOSE: Byte = 0x8
    const val OPCODE_CONNECTION_PING: Byte = 0x9
    const val OPCODE_CONNECTION_PONG: Byte = 0xA
  }

  var fin = false
  var rsv1 = false
  var rsv2 = false
  var rsv3 = false
  var opcode: Byte = 0
  var hasMask = false
  var payloadLen: Long = 0
  lateinit var maskingKey: ByteArray
  lateinit var payloadData: ByteArray

  fun readFrom(input: BufferedInputStream) {
    val firstByte = readByteOrThrow(input)
    decodeFirstByte(firstByte)
    val secondByte = readByteOrThrow(input)
    decodeSecondByte(secondByte, input)
    maskingKey = if (hasMask) decodeMaskKey(input) else ByteArray(0)
    decodePayloadData(input)
    unmask(maskingKey, payloadData, 0, payloadLen.toInt())
  }

  private fun decodeFirstByte(byte: Byte) {
    val b = byte.toInt()
    fin = (b and 0x80) != 0
    rsv1 = (b and 0x40) != 0
    rsv2 = (b and 0x20) != 0
    rsv3 = (b and 0x10) != 0
    opcode = (b and 0xf).toByte()
  }

  private fun decodeSecondByte(byte: Byte, input: InputStream) {
    val b = byte.toInt()
    hasMask = (b and 0x80) != 0
    payloadLen = decodeLength(b and 0x80.inv(), input)
  }

  /**
   * “负载数据”的长度，以字节为单位：
   * 如果是 0-125，这是负载长度。
   * 如果是 126，之后的 2 字节解释为一个 16 位的无符号整数是负载长度。
   * 如果是 127，之后的 8 字节解释为一个 64 位的无符号整数（最高有效位必须是0）是负载长度。
   * 多字节长度数量以网络字节顺序来表示。
   * 注意，在所有情况下，最小数量的字节必须用于编码长度，
   * 例如，一个124字节长的字符串的长度不能被编码为序列126，0，124。
   * 负载长度是“扩展数据”长度+“应用数据”长度。
   * “扩展数据”长度可能是零，在这种情况下，负载长度是“应用数据”长度。
   */
  private fun decodeLength(firstLen: Int, input: InputStream): Long {
    when (firstLen) {
      in 0..125 -> return firstLen.toLong()
      126 -> {
        val part1 = ((readByteOrThrow(input).toInt() and 0xff) shl 8).toLong()
        val part2 = (readByteOrThrow(input).toInt() and 0xff).toLong()
        return part1 or part2
      }
      127 -> {
        var part = 0
        for (i in 0 until 8) {
          part = part shl 8
          part = part or readByteOrThrow(input).toInt() and 0xff
        }
        return part.toLong()
      }
      else -> throw IOException("Unexpected length byte: $firstLen")
    }
  }

  private fun decodeMaskKey(input: InputStream): ByteArray {
    val key = ByteArray(4)
    readByteOrThrow(input, key, 0, key.size)
    return key
  }

  private fun decodePayloadData(input: InputStream) {
    payloadData = ByteArray(payloadLen.toInt())
    readByteOrThrow(input, payloadData, 0, payloadLen.toInt())
  }

  fun writeTo(output: BufferedOutputStream) {
    output.write(encodeFirstByte())
    val lengthAndMasksBit = encodeLength(payloadLen).toByteArray()
    if (hasMask) {
      lengthAndMasksBit[0] = lengthAndMasksBit[0] or 0x80.toByte()
    }
    output.write(lengthAndMasksBit, 0, lengthAndMasksBit.size)

    if (hasMask) {
      throw UnsupportedOperationException("Writing masked data not implemented")
    }
    output.write(payloadData, 0, payloadLen.toInt())
  }

  private fun encodeFirstByte(): Int {
    var byte = 0
    if (fin) byte = byte or 0x80
    if (rsv1) byte = byte or 0x40
    if (rsv2) byte = byte or 0x20
    if (rsv3) byte = byte or 0x10
    byte = byte or (opcode.toInt() and 0xf)
    return byte
  }

  private fun encodeLength(len: Long): Array<Byte> {
    when (len) {
      in 0..125 -> return arrayOf(len.toByte())
      in 126..0xff -> return arrayOf(
        126.toByte(),
        ((len shr 8) and 0xff).toByte(),
        (len and 0xff).toByte()
      )
      else -> return arrayOf(
        127.toByte(),
        ((len shr 56) and 0xff).toByte(),
        ((len shr 48) and 0xff).toByte(),
        ((len shr 40) and 0xff).toByte(),
        ((len shr 32) and 0xff).toByte(),
        ((len shr 24) and 0xff).toByte(),
        ((len shr 26) and 0xff).toByte(),
        ((len shr 8) and 0xff).toByte(),
        (len and 0xff).toByte()
      )
    }
  }

  private fun readByteOrThrow(input: InputStream, buf: ByteArray, _offset: Int, _count: Int) {
    var count = _count
    var offset = _offset
    while (count > 0) {
      val n = input.read(buf, offset, count)
      if (n == -1) {
        throw EOFException()
      }
      count -= n
      offset += n
    }
  }

  private fun readByteOrThrow(input: InputStream): Byte {
    val b = input.read()
    if (b == -1) throw EOFException()
    return b.toByte()
  }
}