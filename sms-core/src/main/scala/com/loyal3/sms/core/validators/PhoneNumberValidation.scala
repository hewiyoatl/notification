package com.loyal3.sms.core.validators

import scala.collection.mutable.ListBuffer
import com.loyal3.sms.core.exceptions.InvalidPhoneNumberException


/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 11/15/13
 * Time: 7:34 PM
 * To change this template use File | Settings | File Templates.
 */
trait PhoneNumberValidation {
  /**
   * Valid lenght of the phone number
   */
  val phoneNumberLength = 10

  /**
   * Pattern to validate if the phone number entered contains any letter
   */
  val letterMatchingPattern = "[a-zA-Z]".r

  /**
   * Pattern to validate phones for the north american numbering plan
   */
  val phoneNumberUSAPattern = "^1?[2-9][0-9]{2}[2-9][0-9]{2}[0-9]{4}$".r

  /**
   * Formats the phone number to 10 digit
   * @param phoneNumber
   * @return
   */
  def getFormattedPhoneNumber(phoneNumber: String): String = {
    val errors = validatePhoneNumber(phoneNumber)

    errors.length match {
      case 0 => {

        // Strip all non-digits characters out of the phone number
        val digitPhoneNumber = phoneNumber.replaceAll("[^0-9]", "")
        // If phone number contains a valid country code, then we remove it
        if (digitPhoneNumber.startsWith("1")) {
          digitPhoneNumber.substring(1)
        }
        else {
          digitPhoneNumber
        }
      }
      case len if len > 0 => throw new InvalidPhoneNumberException("Invalid phone number: " + phoneNumber)
    }
  }

  /**
   * Validates the phone number allowing all kind of phone number formats
   * @param phoneNumber Phone number to be formatted
   * @return Phone number in 10 digit format (non including country code)
   */
  def validatePhoneNumber(phoneNumber: String): Array[String] = {
    val errors: ListBuffer[String] = ListBuffer()

    if (phoneNumber == null || phoneNumber.length == 0) {
      errors += "Phone number is null or empty"
      return errors.toArray
    }

    val find = letterMatchingPattern.findFirstMatchIn(phoneNumber)

    // Validate that the phone number does not contain any letters
    if (!find.isEmpty) {
      errors += "The phone number contains letters"
    }

    val phoneNumberDigitsOnly: String = phoneNumber.replaceAll("[^0-9]", "")

    // Finds all valid number according the North American Numbering Plan
    val findUSA = phoneNumberUSAPattern.findFirstIn(phoneNumberDigitsOnly)

    if (findUSA.isEmpty) {
      errors += "The format of the phone number is not compliance with the north american numbering plan"
    }

    return errors.toArray
  }
}


