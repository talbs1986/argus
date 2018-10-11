package utils

import java.net.InetAddress

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

/**
  * this class is a utility class to handle configs
  */
object Configs {

  val config = ConfigFactory.load().resolve()

  val serverId = InetAddress.getLocalHost.getHostName
  val dataNodes = config.getObject("argus.hosts.datanodes").entrySet().toSeq.filter {
    entry => entry.getKey != serverId
  }.map {
    entry => normalizeHost(entry.getValue.unwrapped().asInstanceOf[String])
  }
  val dataPath = config.getString("argus.data.path")

  /**
    * normalize config hosts
    * @param host host to normalize
    * @return normalized string in the form of "http://<host>"
    */
  private def normalizeHost(host : String) : String = {
    var result = host
    if (!result.startsWith("http"))
      result = s"http://$result"
    if (result.endsWith("/"))
      result = result.substring(0,result.lastIndexOf('/') - 1)

    result
  }

}
