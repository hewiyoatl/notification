package com.loyal3.sms.core.util.security

/**
 * Encrypt/Decrypt the given data using advanced encryption system
 */
trait AES {

  val ALGORITHM           = "AES/CBC/PKCS5Padding"
  val ALGORITHM_KEY_TYPE  = "AES"
  val AES_KEY_SIZE        = 256

  /**
   * Encrypts the given data using secret encryption key
   * @param data - value
   */
  protected[security] def encrypt(data: String) : String

  /**
   * Decrypts the encrypted data using the secret key that was used to encrypt this data
   * @param data - value
   */
  protected[security] def decrypt(data: String) : String

}

