package com.loyal3.sms.core.util

import java.util.Locale
import scala.Predef._
import scala.Some

/**
 * Created with IntelliJ IDEA.
 * User: Kevin Ip
 * Date: 11/19/13
 * Time: 4:41 PM
 */
private[util] trait ResourceBundle {

  val bundlePackagePath: String
  val key: String

  private def load(locale: Option[Locale] = None): java.util.ResourceBundle = {
    locale match {
      case Some(locale) => java.util.ResourceBundle.getBundle(bundlePackagePath.replace(".", "/"), locale)
      case _ => java.util.ResourceBundle.getBundle(bundlePackagePath.replace(".", "/"))
    }
  }

  /**
   * Gets localized string based on the given locale
   * Replaces variables ($#) from the given params
   *
   * e.g.
   * key=Hello $1! This is a wonderful $1
   *
   * getString(Locale.US, "world") = "Hello world! This is a wonderful world"
   * getString(Locale.US, "car") = "Hello car! This is a wonderful car"
   *
   * @param locale
   * @param params
   * @return
   */
  def getString(locale: Option[Locale], params: String*): String = {
    val resourceBundle = load(locale)
    var string = resourceBundle.getString(key)

    var i: Int = 1
    for (param <- params) {
      string = string.replace("$" + i, Option(param).getOrElse("null"))
      i = i + 1
    }

    string
  }

  def getString(locale: Locale, params: String*): String = {
    getString(Some(locale), params: _*)
  }

  def getString(params: String*): String = {
    getString(None, params: _*)
  }

  def getString(): String = {
    getString(None)
  }

}

object ResourceBundles {

  object Messages {

    abstract class Message(myKey: String) extends ResourceBundle{
      val bundlePackagePath: String = "messages.MessageBundle"
      val key: String = this.myKey
    }

    object Subscribe extends Message("subscribe")

    object Verify extends Message("verify")

    object Unsubscribe extends Message("unsubscribe")

    object ErrorMissingParam extends Message("errorMissingParam")

    object ErrorServerError extends Message("errorServerError")

    object Confirm extends Message("confirm")

    object ConfirmUpdate extends Message("confirmUpdate")

    object ConfirmInvalid extends Message("confirmInvalid")

    object ConfirmWithoutSubs extends Message("confirmWithoutSubs")

    object GenericResponse extends Message("genericResponse")

    object ErrorTopicMsgBody extends Message("errorTopicMsgBodyNotPresent")

    object MessagesSent extends Message("messagesSent")

    object MessagesNotSent extends Message("messagesNotSent")

    object ErrorInvalidParam extends Message("errorInvalidParam")

    object SubscribeUpdate extends Message("subscribeUpdate")

    object UnsubscribeUpdate extends Message("unsubscribeUpdate")

    object SubscribeInvalid extends Message("subscribeInvalid")

    object ConfirmAfterWindowClose extends Message("confirmAfterWindowClose")
  }

}