package com.loyal3.sms.core.validators

import scala.collection.mutable.ListBuffer


/**
 * Created with IntelliJ IDEA.
 * User: hewi
 * Date: 11/15/13
 * Time: 7:34 PM
 * To change this template use File | Settings | File Templates.
 */
trait SubscriptionValidation extends  PhoneNumberValidation {

  def isSubscriptionValid(topic: String, userId: String, phoneNumber: String): Array[String] = {
    val errors: ListBuffer[String] = ListBuffer()

    if(userId == null || userId.isEmpty)
    {
      errors += "userId is null or empty"
    }

    if(topic == null || topic.isEmpty)
    {
      errors += "topic is null or empty"
    }

    errors.toArray ++ validatePhoneNumber(phoneNumber)
  }

}