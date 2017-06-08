package com.github.cupenya.authorization.model

import spray.json._

trait PermissionModel

case class Permission(
  id: String,
  name: String,
  description: Option[String]
) extends PermissionModel

object PermissionModel extends DefaultJsonProtocol {
  implicit val PermissionFormat = jsonFormat3(Permission)
}