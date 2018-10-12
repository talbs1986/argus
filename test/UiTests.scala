import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.talbs.argus.resources.api.IService
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import utils.Configs

class UiTests extends PlaySpec
  with OneBrowserPerTest
  with GuiceOneServerPerTest
  with HtmlUnitFactory
  with ServerProvider {

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

  "Main page" should {

    "include available hosts" in {

      go to ("http://localhost:" + port)

      Configs.dataNodes.foreach {
        host => pageSource must include (host)
      }
    }

    "not include the datanode that displays the ui" in {

      go to ("http://localhost:" + port)
      pageSource must not include Configs.serverId
    }
  }
}
