package models

import play.api.libs.json.{Format, Json}

case class LogResult(result:Seq[LogInfo])
object LogResult{
  implicit val logResultFormat: Format[LogResult] = Json.format[LogResult]
}
