package com.talbs.argus.resources.client.impl

import com.talbs.argus.resources.api.IService
import com.talbs.argus.resources.api.tos.GetResourceResponse._
import com.talbs.argus.resources.api.tos.PutResourceRequest._
import com.talbs.argus.resources.api.tos.PutResourceResponse._
import com.talbs.argus.resources.api.tos._
import javax.inject.Named
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * http client for resource service
  * @param host host to connect to
  * @param requestTimeoutMS request timeout in ms
  * @param client *implicit* actual http client to make requests
  */
@Named("client")
case class Client protected[client](host : String, requestTimeoutMS : Long)(implicit client: WSClient) extends IService{

  //add shutdown hook to close client
  sys.addShutdownHook(
    close()
  )

  override def getResource(request: GetResourceRequest): Future[GetResourceResponse] = {
    client
      .url(s"$host/api/${request.resourceName}")
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(requestTimeoutMS millisecond)
      .get()
      .map {
        response =>
          response.json.as[GetResourceResponse]
      }.recoverWith {
      case ex  =>
        ex.printStackTrace()
        Future.successful(GetResourceResponse.notFound(""))
    }
  }

  override def putResource(request: PutResourceRequest): Future[PutResourceResponse] = {
    client
      .url(s"$host/api/${request.resourceName}")
      .addHttpHeaders("Content-type" -> "application/json")
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(requestTimeoutMS millisecond)
      .post(Json.toJson(request))
      .map {
        response =>
          response.json.as[PutResourceResponse]
      }.recoverWith {
      case ex  =>
        ex.printStackTrace()
        Future.successful(PutResourceResponse("",-1))
    }
  }

  override def close(): Unit = {
    client.close()
  }

  override def removeResource(request: RemoveResourceRequest): Future[RemoveResourceResponse] = {
    client
      .url(s"$host/api/${request.resourceName}")
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(requestTimeoutMS millisecond)
      .delete()
      .map {
        response =>
          response.json.as[RemoveResourceResponse]
      }.recoverWith {
      case ex  =>
        ex.printStackTrace()
        Future.successful(RemoveResourceResponse(""))
    }
  }

  override def getResourceDirectNode(request: GetResourceRequest): Future[GetResourceResponse] = {
    client
      .url(s"$host/_api/${request.resourceName}")
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(requestTimeoutMS millisecond)
      .get()
      .map {
        response =>
          response.json.as[GetResourceResponse]
      }.recoverWith {
      case ex  =>
        ex.printStackTrace()
        Future.successful(GetResourceResponse.notFound(""))
    }
  }
}