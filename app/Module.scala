import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.talbs.argus.resources.api.IService
import play.api.{Configuration, Environment, Mode}
import services.ResourceService

/**
  * this class defines the IOC injections
  */
class Module(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure() = {
    if (environment.mode != Mode.Test) {
      bind(classOf[IService])
        .annotatedWith(Names.named("service"))
        .to(classOf[ResourceService])
        .asEagerSingleton()
    }


  }

}
