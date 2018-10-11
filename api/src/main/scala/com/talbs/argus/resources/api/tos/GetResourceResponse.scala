package com.talbs.argus.resources.api.tos

import play.api.libs.json.Json

/**
  * TO response of get resource api
  * @param payload the actual resource data
  * @param owner data node that owns the resource
  * @param updateTime the last time the resource was updated
  * @param sender data node that responded to request
  */
case class GetResourceResponse(payload: String, owner : String, updateTime: Long, sender : String)

object GetResourceResponse {
  //implicits for json parse
  implicit val getResponseJsonWrites = Json.writes[GetResourceResponse]
  implicit val getResponseJsonReads = Json.reads[GetResourceResponse]

  /**
    * build a not found response
    * @param sender data node that responded to request
    * @return [[GetResourceResponse]]
    */
  def notFound(sender : String) : GetResourceResponse = GetResourceResponse("","",-1,sender)

}
