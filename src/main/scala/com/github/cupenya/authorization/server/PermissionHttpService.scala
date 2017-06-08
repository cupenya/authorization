package com.github.cupenya.authorization.server

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.util.Timeout
import spray.json.DefaultJsonProtocol
import org.mongodb.scala._

import scala.concurrent.ExecutionContext
import com.github.cupenya.authorization.persistence._
import com.github.cupenya.authorization.model._

sealed trait PermissionListModel

case class PermissionList(
  permissions: List[Permission]
) extends PermissionListModel

object PermissionListModel extends DefaultJsonProtocol {
  implicit val PermissionListFormat = jsonFormat1(PermissionList)
}

trait PermissionHttpService extends Directives with SprayJsonSupport with DefaultJsonProtocol {
  import scala.concurrent.duration._
  import scala.language.postfixOps

  implicit val system: ActorSystem

  implicit def ec: ExecutionContext

  implicit def db: MongoDatabase

  implicit val materializer: Materializer

  implicit val timeout = Timeout(5 seconds)

  private val permissionDao = PermissionDao

  val permissionsRoute =
    pathPrefix("permissions") {
      pathEndOrSingleSlash {
        get {
          complete {
            permissionDao.findAll().map(_.toList).map(PermissionList.apply)
          }
        } ~
          post {
            entity(as[Permission]) { permission =>
              complete {
                permissionDao.save(permission).map { _ =>
                  Map.empty[String, String]
                }
              }
            }
          }
      }
    }
}
