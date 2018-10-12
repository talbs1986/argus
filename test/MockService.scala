import com.google.inject.Singleton
import com.talbs.argus.resources.api.IService
import com.talbs.argus.resources.api.tos._
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future

/**
  * this class represents a mock service and is not thread safe
  */
@Singleton
case class MockService() extends IService with MockFactory {
  var mockService = mock[IService]

  override def removeResource(request: RemoveResourceRequest): Future[RemoveResourceResponse] =
    mockService.removeResource(request)

  override def getResource(request: GetResourceRequest): Future[GetResourceResponse] =
    mockService.getResource(request)

  override def getResourceDirectNode(request: GetResourceRequest): Future[GetResourceResponse] =
    mockService.getResourceDirectNode(request)

  override def putResource(request: PutResourceRequest): Future[PutResourceResponse] =
    mockService.putResource(request)

  override def close(): Unit = mockService.close()
}