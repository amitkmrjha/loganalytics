package models

import play.api.libs.json.{Format, Json}

case class LogInfo(operation:String,filename:String,line_number:Int,name:String)
object LogInfo{
  implicit val logInfoFormat: Format[LogInfo] = Json.format[LogInfo]
}