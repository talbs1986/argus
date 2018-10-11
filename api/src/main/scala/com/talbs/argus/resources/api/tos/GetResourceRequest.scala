package com.talbs.argus.resources.api.tos

import play.api.libs.json.Json

/**
  * TO request of get resource api
  * @param resourceName resource name
  */
case class GetResourceRequest(resourceName : String)

object GetResourceRequest {
  //implicits for json parse
  implicit val getRequestJsonWrites = Json.writes[GetResourceRequest]
  implicit val getRequestJsonReads = Json.reads[GetResourceRequest]
}
