package com.github.cupenya.authorization.persistence

import com.cupenya.common.mongo.MongoConnection

class AuthorizationMongoConnection(uri: String) extends MongoConnection {
  override protected[this] def mongoUri = uri
}