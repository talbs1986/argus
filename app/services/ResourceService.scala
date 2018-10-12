package services

import java.io.{BufferedWriter, File, FileNotFoundException, FileWriter}

import com.talbs.argus.resources.client.ClientFactory
import com.talbs.argus.resources.api.IService
import com.talbs.argus.resources.api.tos._
import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import utils.Configs

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
  * this class represents the resource service
  *
  * @param wsClient     async client for accessing datanodes
  * @param appLifecycle application lifecycle
  */
class ResourceService @Inject()(wsClient: WSClient, appLifecycle: ApplicationLifecycle) extends IService {

  protected val clients: Seq[IService] = Configs.dataNodes.map {
    host => ClientFactory.build(host)(wsClient)
  }

  Logger.info(s"Found clients ${clients.mkString}")

  //build the local data catalog
  protected val localCatalog: mutable.Map[String, PutResourceResponse] = readLocalCatalog()

  //add shutdown hook
  appLifecycle.addStopHook { () =>
    close()
    Future.successful(())
  }

  Logger.info(s"service ${this.getClass} was loaded successfully")

  override def removeResource(request: RemoveResourceRequest): Future[RemoveResourceResponse] = {
    Future {
      //resource found locally then remove it
      if (localCatalog.contains(request.resourceName)) {
        new File(Configs.dataPath + File.separator + request.resourceName).delete()
        localCatalog.remove(request.resourceName)
        Logger.info(s"removed resource ${request.resourceName}")
      }
      RemoveResourceResponse(Configs.serverId)
    }
  }

  private def updateRemoteNodes(request: RemoveResourceRequest): Future[RemoveResourceResponse] = {
    //build futures to remove resource from all data nodes
    val updateFuts = clients.map {
      client => client.removeResource(request)
    }

    //execute and build response
    Future.sequence(updateFuts).map {
      results =>
        results.headOption.getOrElse(RemoveResourceResponse(Configs.serverId))
    }
  }

  override def getResource(request: GetResourceRequest): Future[GetResourceResponse] = {
    readResourceLocally(request,forceCatalogValidation = true).flatMap {
      putRequestOpt =>
        val putRequest = putRequestOpt.get
        //check if resource found locally
        if (putRequestOpt.isDefined) {
          //need to check also remotely because there might be
          //conflicts in the data , and more updated data on different node
          readResourceRemotely(request).flatMap {
            response =>
              //remove from local registry since its stale
              if (response.updateTime >= putRequest.updateTime) {
                removeResource(RemoveResourceRequest.from(request)).map {
                  _ =>
                    GetResourceResponse(response.payload, response.owner, response.updateTime, Configs.serverId)
                }
              }
              else { //update all other nodes to remove from catalog
                updateRemoteNodes(RemoveResourceRequest.from(request)).map {
                  _ =>
                    GetResourceResponse(putRequest.payload, putRequest.owner, putRequest.updateTime, Configs.serverId)
                }
              }
          }
        }
        else //not found locally , only get from other nodes
          readResourceRemotely(request)
    }
  }


  override def putResource(request: PutResourceRequest): Future[PutResourceResponse] = {
    Future {
      val file = new File(Configs.dataPath + File.separator + request.resourceName)
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(Json.toJson[PutResourceRequest](request).toString())
      bw.close()
      val result = PutResourceResponse(Configs.serverId, request.updateTime)
      localCatalog.update(request.resourceName, result)
      Logger.info(s"added resource ${request.resourceName}")
      result
    }
  }

  override def close(): Unit = {
    clients.foreach(_.close())
  }

  override def getResourceDirectNode(request: GetResourceRequest): Future[GetResourceResponse] = {
    //read from local disk
    //return payload or not found
    readResourceLocally(request,forceCatalogValidation = true).map {
      response =>
        if (response.isDefined)
          GetResourceResponse(response.get.payload, response.get.owner, response.get.updateTime, Configs.serverId)
        else
          GetResourceResponse.notFound(Configs.serverId)
    }
  }

  /**
    * read resource from local disk
    *
    * @param request [[GetResourceRequest]]
    * @param forceCatalogValidation validate resource exists in local cache first
    * @return Option [[PutResourceRequest]]
    */
  private def readResourceLocally(request: GetResourceRequest,forceCatalogValidation : Boolean): Future[Option[PutResourceRequest]] = {
    if (!forceCatalogValidation || localCatalog.contains(request.resourceName)) {
      Future {
        val fileContent = Source.fromFile(Configs.dataPath + File.separator + request.resourceName).mkString
        Some(Json.fromJson[PutResourceRequest](Json.parse(fileContent)).get)
      }
    }
    else
      Future.successful(None)
  }

  /**
    * read resource from remote nodes
    *
    * @param request [[GetResourceRequest]]
    * @return Future [[GetResourceResponse]]
    */
  private def readResourceRemotely(request: GetResourceRequest): Future[GetResourceResponse] = {
    //build futures to get resource from nodes
    //note: using special api instead of getResource() to handle recursive request loop
    val requestFuts = clients.map {
      client => client.getResourceDirectNode(request)
    }

    if (requestFuts.nonEmpty) {
      //execute and build response for remote resource request
      Future.sequence(requestFuts).map {
        results =>
          val filteredResults = results.filter {
            result: GetResourceResponse => result.payload.nonEmpty
          }
          //bugfix for scala that resolved in 2.13 maxByOption
          if (filteredResults.isEmpty)
            GetResourceResponse.notFound(Configs.serverId)
          else
            filteredResults.maxBy {
              result: GetResourceResponse => result.updateTime
            }.copy(sender = Configs.serverId)
      }
    }
    else //no other data nodes , return not found
      Future.successful(GetResourceResponse.notFound(Configs.serverId))
  }

  /**
    * read the entire local catalog
    * @return local catalog
    */
  private def readLocalCatalog(): mutable.Map[String, PutResourceResponse] = {
    //validate data path is correct
    val directory = new File(Configs.dataPath)
    if (!directory.isDirectory)
      throw new Exception("Data config path is not a directory")
    if (!directory.exists())
      throw new Exception("Data config path does not exists")
    if (!directory.canRead)
      throw new Exception("Data config path doesnt have read permissions")
    if (!directory.canWrite)
      throw new Exception("Data config path doesnt have write permissions")

    //build futures for all the local resources found
    val resourcesFuts = directory.listFiles().filter {
      file => file.isFile
    }.map {
      file =>
        readResourceLocally(GetResourceRequest(file.getName),forceCatalogValidation = false).recoverWith {
          case ex =>
            Logger.warn(s"Ignoring resource ${file.getName} due to ${ex.getMessage}")
            Future.successful(None)
        }
    }.toSeq

    //build the local catalog data structure
    if (resourcesFuts.nonEmpty) {
      Await.result(Future.sequence(resourcesFuts).map {
        resources =>
          val resourceEntries: Seq[(String, PutResourceResponse)] = resources.flatten.map {
            resource =>
              (resource.resourceName, PutResourceResponse(resource.owner, resource.updateTime))
          }
          mutable.Map[String, PutResourceResponse](resourceEntries: _*)
      }, Duration.Inf)
    }
    else
      mutable.Map.empty
  }
}
