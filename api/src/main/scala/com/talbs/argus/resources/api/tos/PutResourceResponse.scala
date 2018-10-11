package com.talbs.argus.resources.api.tos

import play.api.libs.json.Json

/**
  * TO response of put resource api
  * @param owner the data node that owns the resource
  * @param updateTime the last time the resource was updated
  */
case class PutResourceResponse(owner : String, updateTime: Long)



object PutResourceResponse {
  //implicits for json parse
  implicit val putResponseJsonWrites = Json.writes[PutResourceResponse]
  implicit val putResponseJsonReads = Json.reads[PutResourceResponse]

}
