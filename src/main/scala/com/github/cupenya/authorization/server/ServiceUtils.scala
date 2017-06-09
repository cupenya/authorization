package com.github.cupenya.authorization.server

import akka.http.scaladsl.model.StatusCode
import spray.json.JsonFormat

sealed trait ServiceApi

object ServiceApi extends spray.json.DefaultJsonProtocol {
  implicit val errorDataFormat = jsonFormat3(ErrorData)
  implicit val serviceResponseErrorFormat = jsonFormat2(ServiceResponseError)
}

/**
 * An base trait for a valid response status for a ServiceResponse.
 */
sealed trait ResponseStatus extends ServiceApi

object ResponseStatus {
  /**
   * When a resource could not be found.
   */
  case object NotFound extends ResponseStatus

  /**
   * When the request was processed OK.
   */
  case object Ok extends ResponseStatus

  /**
   * When the user is not authorized.
   */
  case object Unauthorized extends ResponseStatus

  /**
   * When the user is authorized, but does not have permissions to do something.
   */
  case object Forbidden extends ResponseStatus

  /**
   * When the request was not processed but for a non-technical reason.
   */
  case object NotOk extends ResponseStatus
}

case class ServiceResponse[T: JsonFormat](
  success: Boolean,
  data: Option[T],
  error: Option[ServiceResponseError]
)
    extends ServiceApi

case class ServiceResponseError(
  message: String,
  data: Option[List[ErrorData]]
)
    extends ServiceApi

case class ErrorData(
  field: String,
  message: String,
  messageCode: String
)
    extends ServiceApi
