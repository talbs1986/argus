package com.talbs.argus.resources.api

import com.talbs.argus.resources.api.tos._

import scala.concurrent.Future


/**
  * this interface defines the api for the Resource service
  */
trait IService {

  /**
    * remove resource from local catalog
    * @param request [[RemoveResourceRequest]] to remove resource
    * @return Future [[RemoveResourceResponse]]
    */
  def removeResource(request: RemoveResourceRequest): Future[RemoveResourceResponse]

  /**
    * get resource
    * @param request [[GetResourceRequest]] to get resource
    * @return Future [[GetResourceResponse]]
    */
  def getResource(request: GetResourceRequest): Future[GetResourceResponse]

  /**
    * get resource from specific data node
    * @param request [[GetResourceRequest]] to get resource
    * @return Future [[GetResourceResponse]]
    */
  def getResourceDirectNode(request: GetResourceRequest): Future[GetResourceResponse]

  /**
    * add resource
    * @param request [[PutResourceRequest]] to add resource
    * @return Future [[PutResourceResponse]]
    */
  def putResource(request: PutResourceRequest): Future[PutResourceResponse]

  /**
    * close and clean up
    */
  def close()

}
