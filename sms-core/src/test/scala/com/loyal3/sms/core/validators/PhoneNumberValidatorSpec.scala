package com.loyal3.sms.core.validators

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import com.loyal3.sms.core.exceptions.InvalidPhoneNumberException

/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 11/17/13
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
class PhoneNumberValidatorSpec extends SelfAwareSpecification with PhoneNumberValidation {
  "Phone Validator" should {
    "validate area code valid number" in {
      val phoneNumber = "+ 1 (415) - 235.8283"


      val finalPhoneNumber = getFormattedPhoneNumber(phoneNumber)

      finalPhoneNumber.length mustEqual (10)
      finalPhoneNumber.forall(_.isDigit) mustEqual (true)
    }

    "validate without area code valid number" in {
      val phoneNumber = "+(415) - 235.8283"


      val finalPhoneNumber = getFormattedPhoneNumber(phoneNumber)

      finalPhoneNumber.length mustEqual (10)
      finalPhoneNumber.forall(_.isDigit) mustEqual (true)
    }

    "validate phone number length" in {
      val phoneNumber = "(415) - 2325.8283"

      val errors = validatePhoneNumber(phoneNumber)

      errors.length mustNotEqual (0)
    }
  }

  "validate that phone number does not contain letters" in {
    val phoneNumber = "+(415) - 23a.8283"

    val errors = validatePhoneNumber(phoneNumber)

    errors.length mustNotEqual (0)
  }

  "validate that phone number is not empty" in {
    val phoneNumber: String = ""
    var message = ""

    try {
      val finalPhoneNumber: String = getFormattedPhoneNumber(phoneNumber)
    }
    catch {
      case e: InvalidPhoneNumberException => message = e.getMessage
    }

    message mustNotEqual ("")
  }

  "validate that phone number is not null " in {
    val phoneNumber: String = null

    val errors = validatePhoneNumber(phoneNumber)

    errors.length mustNotEqual (0)
  }

  "validate that phone number is compliance with NANP " in {
    val phoneNumber: String = "1(623)-235-4234"

    val errors = validatePhoneNumber(phoneNumber)

    // No errors found, format is OK
    errors.length mustEqual (0)
  }

  "validate that phone number is compliance with NANP " in {
    val phoneNumber: String = "+1(623)-235-4234"

    val errors = validatePhoneNumber(phoneNumber)

    // No errors found, format is OK
    errors.length mustEqual (0)
  }

  "validate that phone number is not compliance with NANP " in {
    val phoneNumber: String = "1(623)-135-4234"

    val errors = validatePhoneNumber(phoneNumber)

    // Errors found, format is invalid
    errors.length mustNotEqual (0)
  }

  "validate that phone number is not compliance with NANP " in {
    val phoneNumber: String = "1(123)-135-4234"

    val errors = validatePhoneNumber(phoneNumber)

    // Errors found, format is invalid
    errors.length mustNotEqual (0)
  }
}