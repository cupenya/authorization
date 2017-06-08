package com.github.cupenya.authorization.server

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.util.Timeout
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext
import com.github.cupenya.service.discovery._

sealed trait PermissionListModel

case class PermissionList(
  permissions: List[Permission]
) extends PermissionListModel

object PermissionListModel extends DefaultJsonProtocol {
  implicit val PermissionListFormat = jsonFormat1(PermissionList)
}

trait PermissionHttpService extends Directives with SprayJsonSupport {
  import scala.concurrent.duration._
  import scala.language.postfixOps

  implicit val system: ActorSystem

  implicit def ec: ExecutionContext

  implicit val materializer: Materializer

  implicit val timeout = Timeout(5 seconds)

  val permissionsRoute =
    pathPrefix("permissions") {
      pathEndOrSingleSlash {
        get {
          complete {
            PermissionList(Nil)
          }
        }
      }
    }
}
