package com.loyal3.sms.core.util.security

import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import org.apache.commons.codec.binary.Base64

/**
 * Default implementation of the AES algorithm
 */
class DefaultAES extends AES {

  private lazy val aesKey = Array[Byte](8, -123, 41, -5, -103, 54, 72, 5, 106, -54, 52, -78, 80, -37, -26, 46, 35, 50, 78, -121, -70, -88, 49, 80, 79, -53, 77, -36, 109, -7, -125, 93)
  private lazy val keySpec = new SecretKeySpec(aesKey, ALGORITHM_KEY_TYPE)
  private lazy val ivSpec =  new IvParameterSpec(Seq(
    0xd0, 0xc0, 0x7b, 0x3e, 0xd1, 0x8e, 0x6f, 0xfa,
    0xdd, 0x7a, 0x2a, 0xdd, 0xa4, 0x73, 0xd3, 0xf4
  ).map(_.toByte).toArray)

  override protected[security] def encrypt(data: String): String = {
    if (data == null) {
      null
    } else {
      val cipher = Cipher.getInstance(ALGORITHM)
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
      val encryptedBytes = cipher.doFinal(data.getBytes)
      val encrypted = new String(Base64.encodeBase64(encryptedBytes), "UTF-8")
      encrypted
    }
  }

  override protected[security] def decrypt(data: String): String = {
    if (data == null) {
      null
    } else {
      val cipher = Cipher.getInstance(ALGORITHM)
      val decoded = Base64.decodeBase64(data.getBytes("UTF-8"))
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
      val decryptedBytes = cipher.doFinal(decoded)
      val decrypted = new String(decryptedBytes)
      decrypted
    }
  }


}
