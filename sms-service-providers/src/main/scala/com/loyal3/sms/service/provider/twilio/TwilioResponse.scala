package com.loyal3.sms.service.provider.twilio

import com.fasterxml.jackson.annotation.JsonProperty
import com.loyal3.sms.service.provider.{SimpleSendResponse, SendResponse}

case class TwilioResponse(@JsonProperty("date_created") dateCreated: String,
                          @JsonProperty("date_sent") dateSent: String,
                          @JsonProperty("date_updated") dateUpdated: String,
                          @JsonProperty("status") status: String,
                          @JsonProperty("uri") uri: String,
                          @JsonProperty("to") phoneNumber: String,
                          @JsonProperty("sid") sid: String,
                          @JsonProperty("code") code: String) {
  def toSendResponse: SendResponse = new SimpleSendResponse(status = status, code = code)
}
