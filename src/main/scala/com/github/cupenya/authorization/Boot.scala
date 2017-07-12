package com.github.cupenya.authorization

import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import com.github.cupenya.authorization.health._
import com.github.cupenya.service.discovery._
import com.github.cupenya.authorization.persistence._
import com.github.cupenya.authorization.server._
import com.github.cupenya.authorization.model.{ Permission => PermissionEntity }

import com.cupenya.common.mongo._
import scala.util._
import scala.concurrent.Future

object Boot extends App
    with PermissionHttpService
    with HealthCheckRoute
    with HealthCheckService
    with CorsRoute {

  private val mongoConnection = new AuthorizationMongoConnection(Config.database.mongoUri)
  implicit val db = mongoConnection.getDefaultDb
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  private val interface = Config.app.interface
  private val port = Config.app.port

  val mainRoute =
    defaultCORSHeaders {
      options {
        complete(StatusCodes.OK -> None)
      } ~ permissionsRoute
    }

  log.info(s"Starting Authorization service using interface $interface and port $port")

  Http().bindAndHandle(mainRoute, interface, port).transform(
    binding => log.info(s"REST interface bound to ${binding.localAddress} "), { t => log.error(s"Couldn't start Authorization service", t); sys.exit(1) }
  )

  private def handleServiceUpdates[T <: ServiceUpdate](allServiceUpdates: List[T]) = {
    val serviceUpdates = allServiceUpdates.filter { upd =>
      Config.integration.kubernetes.namespaces.isEmpty || Config.integration.kubernetes.namespaces.contains(upd.namespace)
    }

    serviceUpdates.collect {
      case serviceUpdate if !serviceUpdate.permissions.isEmpty =>
        log.info(s"Found permissions ${serviceUpdate.permissions}")
        Future.sequence(serviceUpdate.permissions.map { permission =>
          PermissionDao.save(PermissionEntity(permission.id, permission.name, permission.description))
        }).onComplete {
          case Success(_) =>
            log.info(s"Successfully updated ${serviceUpdate.permissions.size} permissions")
          case Failure(t) =>
            log.error("Failed to update permissions", t)
        }
    }
  }

  val serviceDiscoveryAgent =
    //        system.actorOf(Props(new ServiceDiscoveryAgent[StaticServiceUpdate](new StaticServiceListSource)))
    system.actorOf(Props(new ServiceDiscoveryAgent[KubernetesServiceUpdate](new KubernetesServiceDiscoveryClient, handleServiceUpdates)))

  serviceDiscoveryAgent ! ServiceDiscoveryAgent.WatchServices

  override def checks: List[HealthCheck] = List(new ServiceDiscoveryHealthCheck(serviceDiscoveryAgent))
}
