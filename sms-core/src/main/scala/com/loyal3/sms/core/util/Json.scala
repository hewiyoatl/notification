package com.loyal3.sms.core.util

import com.fasterxml.jackson.databind.{SerializationFeature, DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.text.SimpleDateFormat
import com.fasterxml.jackson.annotation.JsonInclude.Include

object Json {
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
  val mapper = {
    val m = new ObjectMapper()
    m.registerModule(DefaultScalaModule)
    m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    m.setDateFormat(dateFormatter)
    m.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
    m.configure(SerializationFeature.WRAP_EXCEPTIONS, true)
    m.configure(SerializationFeature.INDENT_OUTPUT, true)
    m.setSerializationInclusion(Include.NON_NULL)
    m.setSerializationInclusion(Include.NON_EMPTY)
    m
  }
}
