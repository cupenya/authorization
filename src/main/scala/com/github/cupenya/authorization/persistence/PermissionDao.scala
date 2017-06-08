package com.github.cupenya.authorization.persistence

import com.cupenya.common.mongo.dao._
import com.github.cupenya.authorization.model._

trait PermissionDao extends MongoDao[Permission] {
  override protected val collectionName = "Permission"

  override implicit def jsonFormat = jsonFormat3(Permission)
}

object PermissionDao extends PermissionDao