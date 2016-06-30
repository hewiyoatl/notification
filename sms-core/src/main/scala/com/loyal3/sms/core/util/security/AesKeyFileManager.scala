package com.loyal3.sms.core.util.security

import javax.crypto.{KeyGenerator, SecretKey}
import util.Random

/**
 * Aes key generator. This must be use only once to get a new encryption key which can be used in the encryption key in DefaultAES.
 */
object AesKeyFileManager extends DefaultAES {

  /**
   * Generate an AES secret key
   * @param size  Key size
   * @return      The secret key
   */
  def generateKey(size: Int = AES_KEY_SIZE): SecretKey = {
    val keyGenerator = KeyGenerator.getInstance(ALGORITHM_KEY_TYPE)
    keyGenerator.init(size)
    keyGenerator.generateKey()
  }

  /**
   * Generate a new random initialization vector
   * @return      A random byte array of size 16
   */
  def generateIv(): Array[Byte] = {
    val iv = new Array[Byte](16)
    Random.nextBytes(iv)
    iv
  }
}
