package com.loyal3.sms.core.util.security

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification

/**
  */
class EcnryptDecryptUtilSpec extends SelfAwareSpecification {

  "decrypt and mask function" should {
    "return the original data masked" in {
      val input = "5105095029"
      val encrypted = EncryptDecryptUtil.encrypt(input)
      val masked = EncryptDecryptUtil.decryptAndMask(encrypted)
      masked mustNotEqual (input)
      masked.count(_ == '*') mustEqual (6)
      masked.dropWhile(_ == '*') mustEqual ("5029")
    }
  }

  "decrypt and mask function for custom expose value" should {
    "return the original data masked" in {
      val input = "5105095029"
      val encrypted = EncryptDecryptUtil.encrypt(input)
      val masked = EncryptDecryptUtil.decryptAndMask(encrypted, 0)
      masked mustNotEqual (input)
      masked.count(_ == '*') mustEqual (10)
      masked.dropWhile(_ == '*') mustEqual ("")
    }
  }

  "decrypt and mask function for expose value higher than original value size" should {
    "throws illegal argument exception" in {
      val input = "5105095029"
      val encrypted = EncryptDecryptUtil.encrypt(input)
      EncryptDecryptUtil.decryptAndMask(encrypted, 11) must throwA[IllegalArgumentException]
    }
  }

}
