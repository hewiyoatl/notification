package com.loyal3.sms.core.util.security

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification

/**
 *
 */
class DefaultAESSpec extends SelfAwareSpecification {

  "encrypt the given text" should {
    "return the encrypted data" in {
      val input = "5105095029"
      val defaultAES = new DefaultAES
      val encrypted = defaultAES.encrypt(input)

      encrypted mustNotEqual(input)

      val decrypted = defaultAES.decrypt(encrypted)

      decrypted mustNotEqual(encrypted)
      decrypted mustEqual(input)
    }
  }

  "encrypt the given text twice" should {
    "return the same ecrypted value" in {
      val input = "5105095029"
      val defaultAES = new DefaultAES
      val encrypted1 = defaultAES.encrypt(input)
      encrypted1 mustNotEqual(input)

      val encrypted2 = defaultAES.encrypt(input)
      encrypted2 mustEqual(encrypted1)
    }
  }

}
