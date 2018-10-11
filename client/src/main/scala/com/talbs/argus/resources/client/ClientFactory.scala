package com.talbs.argus.resources.client

import com.talbs.argus.resources.client.impl.Client
import com.talbs.argus.resources.api.IService
import play.api.libs.ws.WSClient

import scala.collection.mutable

/**
  * this factory class builds a client and cache it by host
  */
object ClientFactory {

  private val clientCache : mutable.Map[String,IService] = mutable.Map[String,IService]()

  /**
    * builds an http client and cache it
    *
    * @param host host to connect
    * @param requestTimeoutMS request timeout ms
    * @param client *implicit* impl of http client to make request
    * @return http client that implements [[IService]]
    */
  def build(host : String,requestTimeoutMS : Long = 3000)(implicit client : WSClient) : IService = {

    clientCache.getOrElse(host,{
      clientCache.synchronized {
        clientCache.getOrElseUpdate(host,Client(host,requestTimeoutMS)(client))
      }
    })

  }

}
