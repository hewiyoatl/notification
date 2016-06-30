package com.loyal3.sms.service.provider.twilio

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification

class TwilioProviderSpec  extends SelfAwareSpecification {
  "TwilioProvider" should {
    val provider = new TwilioProvider("AC387f15c74e900353826a29a69721292a","b8cba54896356911a77de58e61c6d99b" )
    "validate signature from twilio docs https://www.twilio.com/docs/api/security" in {

      val params = Map(
        "CallSid" -> "CA1234567890ABCDE",
        "Caller" -> "+14158675309",
        "Digits" -> "1234",
        "From" -> "+14158675309",
        "To" -> "+18005551212")
      provider.validateRequest("YmfLx6bb2gw2WKIKr2ootlrinvo=","https://mycompany.com/myapp.php?foo=1&bar=2", params ) must not throwA
    }

    "fail validation for incorrect signatures" in {

      val params = Map(
        "CallSid" -> "CA1234567890ABCDE",
        "Caller" -> "+14158675309",
        "Digits" -> "1234",
        "From" -> "+14158675309",
        "To" -> "+18005551212")
      provider.validateRequest("YmfLx6bb2gw2IKr2ootlrinvo=","https://mycompany.com/myapp.php?foo=1&bar=2", params ) must throwA[SecurityException]
      provider.validateRequest("YmfLx6bb2gw992WKIKr2ootlrinvo=","https://mycompany.com/myapp.php?foo=1&bar=2", params ) must throwA[SecurityException]
      provider.validateRequest("","https://mycompany.com/myapp.php?foo=1&bar=2", params ) must throwA[SecurityException]
      provider.validateRequest("","https://mycompany.com/myapp.php?foo=1&bar=2", params ) must throwA[SecurityException]
      provider.validateRequest("YmfLx6bb2gw2WKIKr2ootlrinvo=","https://mycompany.com/myapp.php", params ) must throwA[SecurityException]
      provider.validateRequest("YmfLx6bb2gw2WKIKr2ootlrinvo=","https://mycompany.com/myapp", params ) must throwA[SecurityException]
      provider.validateRequest("YmfLx6bb2gw2WKIKr2ootlrinvo=","https://mycompany.com/myapp.php?foo=1&bar=2", Map() ) must throwA[SecurityException]
      provider.validateRequest("YmfLx6bb2gw2WKIKr2ootlrinvo=","https://mycompany.com/myapp.php?foo=1&bar=2", Map("some"-> "param") ) must throwA[SecurityException]
      provider.validateRequest("YmfLx6bb2gw2WKIKr2ootlrinvo=","https://mycompany.com/myapp.php?foo=1&bar=2", params + ("some" -> "param") ) must throwA[SecurityException]
    }
  }

}
