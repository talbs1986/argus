package com.talbs.argus.resources.api.tos

import play.api.libs.json.Json

/**
  * TO request to remove resource api
  * @param resourceName resource name
  */
case class RemoveResourceRequest(resourceName : String)

object RemoveResourceRequest {

  /**
    * build a remove resource request from get request
    * @param request [[GetResourceRequest]]
    * @return [[RemoveResourceRequest]]
    */
  def from(request : GetResourceRequest) : RemoveResourceRequest =
    RemoveResourceRequest(request.resourceName)

  //implicits for json parse
  implicit val removeRequestJsonWrites = Json.writes[RemoveResourceRequest]
  implicit val removeRequestJsonReads = Json.reads[RemoveResourceRequest]
}
