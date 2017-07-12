package com.github.cupenya.authorization.server

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.util.Timeout
import com.mongodb.casbah.Imports._
import spray.json.DefaultJsonProtocol

import spray.json._

import scala.concurrent.ExecutionContext
import com.github.cupenya.authorization.persistence._
import com.github.cupenya.authorization.model._

sealed trait PermissionListModel

trait Protocols extends DefaultJsonProtocol {
  implicit def serviceResponseFormat[T: JsonFormat] = jsonFormat3(ServiceResponse.apply[T])
}

trait PermissionHttpService extends Directives with SprayJsonSupport with Protocols {
  import scala.concurrent.duration._
  import scala.language.postfixOps

  implicit val system: ActorSystem

  implicit def ec: ExecutionContext

  implicit def db: MongoDB

  implicit val materializer: Materializer

  implicit val timeout = Timeout(5 seconds)

  private val permissionDao = PermissionDao

  val permissionsRoute =
    pathPrefix("permissions") {
      pathEndOrSingleSlash {
        get {
          complete {
            permissionDao.findAll().map(apiComplete(_))
          }
        } ~
          post {
            entity(as[Permission]) { permission =>
              complete {
                permissionDao.save(permission).map { _ =>
                  apiComplete(Map.empty[String, String])
                }
              }
            }
          }
      }
    }
}
