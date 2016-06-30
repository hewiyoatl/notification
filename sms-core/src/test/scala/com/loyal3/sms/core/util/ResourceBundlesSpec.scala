package com.loyal3.sms.core.util

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import org.scalatest.mock.MockitoSugar
import java.util.Locale

/**
 * Created with IntelliJ IDEA.
 * User: Kevin Ip
 * Date: 11/19/13
 * Time: 4:41 PM
 */
class ResourceBundlesSpec extends SelfAwareSpecification with MockitoSugar {

  abstract class Test(myKey: String) extends ResourceBundle {
    val bundlePackagePath: String = "messages.TestBundle"
    val key: String = this.myKey
  }

  object NoParam extends Test("noParam")

  object OneParam extends Test("oneParam")

  object TwoParams extends Test("twoParams")

  object RepeatParams extends Test("repeatParams")

  "Message NoParam" should {
    "displays in English by default" in {

      val message = NoParam.getString
      message mustEqual "No param"
    }

    "displays in UK English if Locale.UK" in {
      val message = NoParam.getString(Locale.UK)

      message mustEqual "No param in GB"
    }
    "displays in default if unknown locale" in {
      val message = NoParam.getString(new Locale("foo", "bar"))

      message mustEqual "No param"
    }

    "displays in default if providing parameters" in {
      val message = NoParam.getString("foo", "bar")

      message mustEqual "No param"
    }
  }

  "Message TwoParams" should {
    "displays in English without param" in {
      val message = TwoParams.getString
      message mustEqual "param $1 $2"
    }

    "displays in English with param" in {
      val message = TwoParams.getString("foo", "bar")
      message mustEqual "param foo bar"
    }

    "displays in UK English with Locale.UK and param" in {
      val message = TwoParams.getString(Locale.UK, "foo", "bar")
      message mustEqual "param foo bar in GB"
    }

    "displays in UK English with Locale.UK and lots of params" in {
      val message = TwoParams.getString(Locale.UK, "foo", "bar", "baz", "blah")
      message mustEqual "param foo bar in GB"
    }

    "displays in UK English with Locale.UK and null params" in {
      val message = TwoParams.getString(Locale.UK, null, null)
      message mustEqual "param null null in GB"
    }

  }

  "Message RepeatParams" should {
    "displays in English without param" in {
      val message = RepeatParams.getString
      message mustEqual "param $1 $1"
    }

    "displays in English with param" in {
      val message = RepeatParams.getString("foo")
      message mustEqual "param foo foo"
    }

    "displays in UK English with Locale.UK and param" in {
      val message = RepeatParams.getString(Locale.UK, "foo", "bar")
      message mustEqual "param foo foo in GB"
    }

    "displays in UK English with Locale.UK and lots of params" in {
      val message = RepeatParams.getString(Locale.UK, "foo", "bar", "baz", "blah")
      message mustEqual "param foo foo in GB"
    }

    "displays in UK English with Locale.UK and null params" in {
      val message = RepeatParams.getString(Locale.UK, null)
      message mustEqual "param null null in GB"
    }

  }

}