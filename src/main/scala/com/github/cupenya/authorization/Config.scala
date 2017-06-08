package com.github.cupenya.authorization

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

object Config {
  private val rootConfig = ConfigFactory.load()

  object database {
    private val config = rootConfig.getConfig("database")
    val mongoUri = config.getString("mongoUri")
  }

  object app {
    private val config = rootConfig.getConfig("app")
    val interface = config.getString("interface")
    val port = config.getInt("port")
  }

  object integration {
    private val config = rootConfig.getConfig("integration")

    object kubernetes {
      private val k8sConfig = config.getConfig("kubernetes")
      val host = k8sConfig.getString("host")
      val port = k8sConfig.getInt("port")
      val token = k8sConfig.getString("token")
      val namespaces = k8sConfig.getStringList("namespaces").toList
    }
  }
}
