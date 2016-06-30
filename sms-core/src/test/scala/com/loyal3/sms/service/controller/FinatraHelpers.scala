package com.loyal3.sms.service.controller

import com.twitter.finatra.{AppService, ControllerCollection, Controller}
import com.twitter.util.{Await, Future}
import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.buffer.ChannelBuffers
import com.twitter.finatra.test.MockResponse
import com.loyal3.sms.core.util.Json

trait FinatraHelpers {
  def usingController(controller: Controller): TestableController = TestableController(controller)
}

case class TestableController(controller: Controller) {
  var lastResponse: Future[FinagleResponse] = null

  def response = new FinatraResponse(Await.result(lastResponse))

  def buildRequest(method: HttpMethod, path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null) {
    if (body != null && params != null && !params.isEmpty) {
      throw new RuntimeException("you can either pass a body OR url encoded parameters, but not both!")
    }

    val request = FinagleRequest(path, params.toList: _*)

    if (body != null) {
      val content: Array[Byte] = body match {
        case s: String => s.getBytes
        case b: Array[Byte] => b
        case obj => Json.mapper.writeValueAsString(obj).getBytes
      }

      request.setHeader("Content-Length", content.length.toString)
      request.setContent(ChannelBuffers.wrappedBuffer(content))
    }

    request.httpRequest.setMethod(method)
    headers.foreach {
      header => request.httpRequest.setHeader(header._1, header._2)
    }

    val collection = new ControllerCollection
    collection.add(controller)

    val appService = new AppService(collection)

    lastResponse = appService(request)
  }

  def get(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map()) {
    buildRequest(HttpMethod.GET, path, params, headers)
  }

  def post(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null) {
    buildRequest(HttpMethod.POST, path, params, headers, body)
  }

  def put(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null) {
    buildRequest(HttpMethod.PUT, path, params, headers, body)
  }

  def delete(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map()) {
    buildRequest(HttpMethod.DELETE, path, params, headers)
  }

  def head(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map()) {
    buildRequest(HttpMethod.HEAD, path, params, headers)
  }

  def patch(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null) {
    buildRequest(HttpMethod.PATCH, path, params, headers, body)
  }

  def options(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map()) {
    buildRequest(HttpMethod.OPTIONS, path, params, headers)
  }
}

class FinatraResponse(response: FinagleResponse) extends MockResponse(response) {
  def readValue[T](implicit t: Manifest[T]): T = {
    Json.mapper.readValue(response.getContentString(), t.runtimeClass).asInstanceOf[T]
  }
}

