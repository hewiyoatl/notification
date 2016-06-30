package com.loyal3.sms.core.util.security

import scala.StringBuilder
import scala.Predef._
import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64


class EncryptDecryptUtil {
  lazy val aes = new DefaultAES
}

object EncryptDecryptUtil extends EncryptDecryptUtil {

  /**
   * Decrypts the value and then masks the value with asterisk.
   */
  def decryptAndMask(encryptedValue: String, exposeLastN: Int = 4): String = {

    val decryptedValue = decrypt(encryptedValue)
    require(exposeLastN < decryptedValue.length, "The number of characters to be masked is more than size of the actual value")

    val startExpose: Int = decryptedValue.length - exposeLastN
    val builder = new StringBuilder(decryptedValue.length)
    for (index <- 0 until startExpose) {
      builder.append("*")
    }
    builder.append(decryptedValue.substring(startExpose))
    builder.toString()
  }


  def digest(text: String): String = {
    val bytes: Array[Byte] = text.getBytes
    digest(bytes)
  }

  def digest(bytes: Array[Byte]): String = {
    val digester = MessageDigest.getInstance("SHA-256")
    val digest = digester.digest(bytes)
    new String(Base64.encodeBase64(digest), "UTF-8")
  }

  /**
   * Encrypt the data
   * @return - array of bytes
   */
  def encrypt(value:String): String = Option(value).map(aes.encrypt(_)).getOrElse(throw new RuntimeException("cannot encrypt the value %s".format(value)))

  /**
   * Decrypt the data
   * @return - array of bytes
   */
  def decrypt(value:String): String = Option(value).map(aes.decrypt(_)).getOrElse(throw new RuntimeException("cannot decrypt the value %s".format(value)))
}
