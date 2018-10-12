import java.io.File

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.talbs.argus.resources.api.IService
import com.talbs.argus.resources.api.tos.{GetResourceRequest, _}
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, OneInstancePerTest}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import services.ResourceService
import utils.Configs

import scala.concurrent.Await
import scala.concurrent.duration._

class ResourcesServiceTests extends PlaySpec with GuiceOneAppPerSuite with Matchers with MockFactory with BeforeAndAfterEach with OneInstancePerTest {

  Configs.dataNodes = Seq()
  private val resourceName = "resource"

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .bindings(new AbstractModule {
        override def configure() = {
          bind(classOf[IService])
            .annotatedWith(Names.named("service"))
            .to(classOf[ResourceService])
            .asEagerSingleton()
        }
      })
      .overrides(bind[WSClient].to[MockWsClient])
      .build()
  }

  override def beforeEach(): Unit = clean()

  override def afterEach(): Unit = clean()


  "ResourcesService" should {

    "getResource" in {
      val service = app.injector.instanceOf[ResourceService]
      var actual = Await.result(service.getResource(GetResourceRequest(resourceName)), 10 seconds)
      actual mustBe GetResourceResponse.notFound(Configs.serverId)

      val data = PutResourceRequest(resourceName, "{}", Configs.serverId, System.currentTimeMillis())
      Await.result(service.putResource(data), 10 seconds)

      actual = Await.result(service.getResource(GetResourceRequest(resourceName)), 10 seconds)
      actual.updateTime mustBe data.updateTime
      actual.payload mustBe data.payload
      actual.owner mustBe data.owner
      actual.sender mustBe data.owner

    }

    "getResourceDirectNode" in {
      val service = app.injector.instanceOf[ResourceService]
      var actual = Await.result(service.getResourceDirectNode(GetResourceRequest(resourceName)), 10 seconds)
      actual mustBe GetResourceResponse.notFound(Configs.serverId)

      val data = PutResourceRequest(resourceName, "{}", Configs.serverId, System.currentTimeMillis())
      Await.result(service.putResource(data), 10 seconds)

      actual = Await.result(service.getResourceDirectNode(GetResourceRequest(resourceName)), 10 seconds)
      actual.updateTime mustBe data.updateTime
      actual.payload mustBe data.payload
      actual.owner mustBe data.owner
      actual.sender mustBe data.owner

    }

    "putResource" in {
      val service = app.injector.instanceOf[ResourceService]

      val data = PutResourceRequest(resourceName, "{}", Configs.serverId, System.currentTimeMillis())
      var putResponse = Await.result(service.putResource(data), 10 seconds)
      putResponse.owner mustBe data.owner
      putResponse.updateTime mustBe data.updateTime

      Thread.sleep(1000)
      val newData = data.copy(updateTime = System.currentTimeMillis())
      putResponse = Await.result(service.putResource(newData), 10 seconds)
      putResponse.owner mustBe data.owner
      putResponse.updateTime mustBe newData.updateTime
    }

    "removeResource" in {
      val service = app.injector.instanceOf[ResourceService]

      var resource = Await.result(service.getResourceDirectNode(GetResourceRequest(resourceName)), 10 seconds)
      resource mustBe GetResourceResponse.notFound(Configs.serverId)

      var actual = Await.result(service.removeResource(RemoveResourceRequest(resourceName)),10 seconds)
      actual.sender mustBe Configs.serverId

      val data = PutResourceRequest(resourceName, "{}", Configs.serverId, System.currentTimeMillis())
      Await.result(service.putResource(data), 10 seconds)
      resource = Await.result(service.getResourceDirectNode(GetResourceRequest(resourceName)), 10 seconds)
      resource.owner mustBe Configs.serverId

      actual = Await.result(service.removeResource(RemoveResourceRequest(resourceName)),10 seconds)
      actual.sender mustBe Configs.serverId

      resource = Await.result(service.getResourceDirectNode(GetResourceRequest(resourceName)), 10 seconds)
      resource mustBe GetResourceResponse.notFound(Configs.serverId)

    }
  }

  private def clean() = {
    val file = new File(Configs.dataPath + File.separator + resourceName)
    if (file.exists())
      file.delete()
  }
}
