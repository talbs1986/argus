package com.talbs.argus.resources.api.tos

import play.api.libs.json.Json

/**
  * TO response of remove resource api
  * @param sender the data node that responded to request
  */
case class RemoveResourceResponse(sender : String)

object RemoveResourceResponse {

  //implicits for json parse
  implicit val removeResponseJsonWrites = Json.writes[RemoveResourceResponse]
  implicit val removeResponseJsonReads = Json.reads[RemoveResourceResponse]
}

