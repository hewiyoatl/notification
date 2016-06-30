package com.loyal3.sms.service.provider.twilio

import com.loyal3.sms.test.support.scopes.SelfAwareSpecification
import com.loyal3.sms.core.util.Json

class TwilioResponseSpec extends SelfAwareSpecification {
  "TwilioResponse" should {
    "ignore unmapped properties" in {
      val json =
        """
          |{
          |   "account_sid": "AC5ef8732a3c49700934481addd5ce1659",
          |   "api_version": "2010-04-01",
          |   "body": "Jenny please?! I love you <3",
          |   "num_segments": "1",
          |   "num_media": "1",
          |   "date_created": "Wed, 18 Aug 2010 20:01:40 +0000",
          |   "date_sent": null,
          |   "date_updated": "Wed, 18 Aug 2010 20:01:40 +0000",
          |   "direction": "outbound-api",
          |   "from": "+14158141829",
          |   "price": null,
          |   "sid": "MM90c6fc909d8504d45ecdb3a3d5b3556e",
          |   "status": "queued",
          |   "to": "+15558675309",
          |   "uri": "/2010-04-01/Accounts/AC5ef8732a3c49700934481addd5ce1659/Messages/MM90c6fc909d8504d45ecdb3a3d5b3556e.json"
          |}
        """.stripMargin

      // When
      val response: TwilioResponse = Json.mapper.readValue(json, classOf[TwilioResponse])

      // Then
      response.status mustEqual "queued"
    }
  }
}
