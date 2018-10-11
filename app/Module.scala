import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.talbs.argus.resources.api.IService
import services.ResourceService

/**
  * this class defines the IOC injections
  */
class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[IService])
      .annotatedWith(Names.named("service"))
      .to(classOf[ResourceService])
      .asEagerSingleton()
  }

}
