package controllers

import akka.actor.ActorSystem
import com.talbs.argus.resources.api.IService
import com.talbs.argus.resources.api.tos.{GetResourceRequest, PutResourceRequest, RemoveResourceRequest}
import javax.inject._
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._
import utils.Configs
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * this controller handles the HTTP requests coming from
  * Forms and API
  *
  * @param cc          standard controller components
  * @param actorSystem actor system to schedule run async requests
  * @param resourceService resource service that implements [[IService]]
  * @param exec        *implicit* execution context to handle async requests
  */
@Singleton
class AppController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem
                              , @com.google.inject.name.Named("service") resourceService: IService)
                             (implicit exec: ExecutionContext, assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  //forms definitions
  private val getResourceForm = Form(
    mapping(
      "resourceName" -> text
    )(GetResourceRequest.apply)(GetResourceRequest.unapply)
  )

  private val putResourceForm = Form(
    mapping(
      "resourceName" -> text,
      "payload" -> text
    )(PutResourceForm.apply)(PutResourceForm.unapply)
  )

  private case class PutResourceForm(resource: String, payload: String)

  /**
    * api call to get resource
    */
  def getResource(resource: String) = Action.async {
    getResourceAsync(resource, 1.millisecond, internalApi = false).map { msg => Ok(msg) }
  }

  /**
    * api call to get resource using direct data node
    */
  def _getResource(resource: String) = Action.async {
    getResourceAsync(resource, 1.millisecond, internalApi = true).map { msg => Ok(msg) }
  }

  /**
    * api call to put resource
    */
  def putResource(resource: String) = Action.async(parse.json) { request =>
    putResourceAsync(resource, request.body.toString(), 1.millisecond).map { msg => Ok(msg) }
  }

  /**
    * api call to remove resource
    */
  def removeResource(resource: String) = Action.async {
    removeResourceAsync(resource, 1.millisecond).map { msg => Ok(msg) }
  }

  /**
    * form call to handle put resource
    */
  def putResourceFromForm = Action.async { implicit request =>
    val dynamicForm = putResourceForm.bindFromRequest.get
    putResourceAsync(dynamicForm.resource, dynamicForm.payload, 1 millisecond)
      .map {
        response => Ok(response)
      }
  }

  /**
    * form call to get resource
    */
  def getResourceFromForm = Action.async { implicit request =>
    val dynamicForm = getResourceForm.bindFromRequest.get
    getResourceAsync(dynamicForm.resourceName, 1 millisecond, false)
      .map {
        response => Ok(response)
      }
  }

  /**
    * form call to display data nodes
    */
  def index = Action {
    Ok(views.html.index(Configs.dataNodes))
  }

  /**
    * async get resource
    * @param resource resource name
    * @param delayTime delay to schedule async block
    * @param internalApi should perform get resource only locally
    * @return [[com.talbs.argus.resources.api.tos.GetResourceResponse]] json string
    */
  private def getResourceAsync(resource: String, delayTime: FiniteDuration, internalApi: Boolean): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      val getResourceMethod = if (internalApi)
        resourceService.getResourceDirectNode(GetResourceRequest(resource))
      else
        resourceService.getResource(GetResourceRequest(resource))
      getResourceMethod.map {
        response =>
          val responseJson = Json.toJson(response).toString()
          promise.success(responseJson)
      }
    }(actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
    promise.future.recoverWith {
      case ex => Future.failed(ex)
    }
  }

  /**
    * async put resource
    * @param resource resource name
    * @param body [[PutResourceRequest]] json string
    * @param delayTime delay to schedule async block
    * @return [[com.talbs.argus.resources.api.tos.PutResourceResponse]] json string
    */
  private def putResourceAsync(resource: String, body: String, delayTime: FiniteDuration): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      resourceService.putResource(PutResourceRequest(resource, body, Configs.serverId)).map {
        response =>
          val responseJson = Json.toJson(response).toString()
          promise.success(responseJson)
      }
    }(actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
    promise.future
  }

  /**
    * async remove resource
    * @param resource resource name
    * @param delayTime delay to schedule async block
    * @return [[com.talbs.argus.resources.api.tos.RemoveResourceResponse]] json string
    */
  private def removeResourceAsync(resource: String, delayTime: FiniteDuration): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      resourceService.removeResource(RemoveResourceRequest(resource)).map {
        response =>
          val responseJson = Json.toJson(response).toString()
          promise.success(responseJson)
      }
    }(actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
    promise.future
  }

}

