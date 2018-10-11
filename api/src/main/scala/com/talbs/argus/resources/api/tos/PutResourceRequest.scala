package com.talbs.argus.resources.api.tos

import play.api.libs.json.Json

/**
  * TO request for put resource api
  * @param resourceName resource name
  * @param payload actual resource data
  * @param owner the data node owner of the resource
  * @param updateTime the last time the resource was updated
  */
case class PutResourceRequest(resourceName : String,payload: String, owner : String, updateTime: Long = System.currentTimeMillis())


object PutResourceRequest {
  //implicits for json parse
  implicit val putRequestJsonWrites = Json.writes[PutResourceRequest]
  implicit val putRequestJsonReads = Json.reads[PutResourceRequest]

}
