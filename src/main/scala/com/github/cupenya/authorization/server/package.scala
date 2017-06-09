package com.github.cupenya.authorization

import akka.http.scaladsl.model.{ HttpHeader, StatusCode, StatusCodes }
import scala.concurrent.{ ExecutionContext, Future }
import spray.json._

package object server {
  import scala.language.implicitConversions

  def serviceSuccess[T: JsonFormat](t: T): ServiceResponse[T] =
    ServiceResponse(success = true, Some(t), None)
  def serviceNoSuccess[T: JsonFormat](message: String): ServiceResponse[T] =
    ServiceResponse(success = false, None, Some(ServiceResponseError(message, None)))
  def serviceUnauthorized[T: JsonFormat](message: String = "No authorization information supplied."): ServiceResponse[T] =
    ServiceResponse(success = false, None, Some(ServiceResponseError(message, None)))

  implicit def toServiceSuccess[T: JsonFormat](t: T): ServiceResponse[T] = serviceSuccess(t)

  def apiComplete[T: JsonFormat](response: Future[ServiceResponse[T]], statusCode: StatusCode)(implicit ec: ExecutionContext): Future[(StatusCode, List[HttpHeader], ServiceResponse[T])] = {
    response.map(apiComplete(_, statusCode = statusCode))
  }

  def apiComplete[T: JsonFormat](apiResponse: ServiceResponse[T], responseHeaders: List[HttpHeader] = Nil, statusCode: StatusCode = StatusCodes.OK): (StatusCode, List[HttpHeader], ServiceResponse[T]) = {
    (statusCode.intValue, responseHeaders, apiResponse)
  }
}