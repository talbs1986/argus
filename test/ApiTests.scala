import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.talbs.argus.resources.api.IService
import com.talbs.argus.resources.api.tos._
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class ApiTests extends PlaySpec with GuiceOneAppPerSuite with Matchers with MockFactory {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .bindings(new AbstractModule {
        override def configure() = {
          bind(classOf[IService])
            .annotatedWith(Names.named("service"))
            .to(classOf[MockService])
            .asEagerSingleton()
        }
      }
      )
      .build()

  "AppController" should {

    "main page" in {
      var home = route(app, FakeRequest(GET, "/")).get
      status(home) mustBe Status.OK

      home = route(app, FakeRequest(POST, "/")).get
      status(home) mustBe Status.NOT_FOUND

      home = route(app, FakeRequest(DELETE, "/")).get
      status(home) mustBe Status.NOT_FOUND

      home = route(app, FakeRequest(PUT, "/")).get
      status(home) mustBe Status.NOT_FOUND
    }


    "api/getResource" in {
      val mockService = app.injector.instanceOf[MockService]
      val expected = GetResourceResponse("", "", System.currentTimeMillis(), "")
      (mockService.mockService.getResource _)
        .expects(*)
        .once()
        .returns(Future.successful(expected))
      val api = route(app, FakeRequest(GET, "/api/someResource")).get
      status(api) mustBe Status.OK
      val response = contentAsJson(api).as[GetResourceResponse]
      response mustBe expected

    }

    "api/putResource" in {
      val mockService = app.injector.instanceOf[MockService]
      val expected = PutResourceResponse("",System.currentTimeMillis())
      val request = "{}"
      (mockService.mockService.putResource _)
        .expects(*)
        .once()
        .returns(Future.successful(expected))
      var api = route(app, FakeRequest(POST, "/api/someResource")
        .withHeaders("Content-type"->"application/json")
        .withBody(request)).get
      status(api) mustBe Status.OK
      val response = contentAsJson(api).as[PutResourceResponse]
      response mustBe expected

      api = route(app, FakeRequest(POST, "/api/someResource")).get
      status(api) mustBe Status.UNSUPPORTED_MEDIA_TYPE

      api = route(app, FakeRequest(POST, "/api/someResource")
        .withHeaders("Content-type"->"application/json")).get
      status(api) mustBe Status.BAD_REQUEST

      api = route(app, FakeRequest(POST, "/api/someResource")
        .withHeaders("Content-type"->"application/json")
      .withBody("{..}")).get
      status(api) mustBe Status.BAD_REQUEST
    }

    "_api/removeResource" in {
      val mockService = app.injector.instanceOf[MockService]
      val expected = RemoveResourceResponse("")
      (mockService.mockService.removeResource _)
        .expects(*)
        .once()
        .returns(Future.successful(expected))
      val api = route(app, FakeRequest(DELETE, "/_api/someResource")).get
      status(api) mustBe Status.OK
      val response = contentAsJson(api).as[RemoveResourceResponse]
      response mustBe expected
    }

    "_api/getResource" in {
      val mockService = app.injector.instanceOf[MockService]
      val expected = GetResourceResponse("", "", System.currentTimeMillis(), "")
      (mockService.mockService.getResourceDirectNode _)
        .expects(*)
        .once()
        .returns(Future.successful(expected))
      val api = route(app, FakeRequest(GET, "/_api/someResource")).get
      status(api) mustBe Status.OK
      val response = contentAsJson(api).as[GetResourceResponse]
      response mustBe expected

    }

  }

}
