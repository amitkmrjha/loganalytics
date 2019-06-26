package controllers

import java.io.File
import java.nio.file.{Files, Path}

import javax.inject._
import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl._
import akka.util.ByteString
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo

import scala.concurrent.{ExecutionContext, Future}

case class LogFormData(name: String)

class LogController @Inject() (cc:MessagesControllerComponents)
                              (implicit executionContext: ExecutionContext,mat:Materializer)
  extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)

  val logForm = Form(
    mapping(
      "name" -> text
    )(LogFormData.apply)(LogFormData.unapply)
  )


  /**
    * Renders a start page.
    */
  def logindex = Action { implicit request =>
    Ok(views.html.logindex(logForm))
  }

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  /**
    * Uses a custom FilePartHandler to return a type of "File" rather than
    * using Play's TemporaryFile class.  Deletion must happen explicitly on
    * completion, rather than TemporaryFile (which uses finalization to
    * delete temporary files).
    *
    * @return
    */
  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType, _) =>
      val path: Path = Files.createTempFile("multipartBody", "tempFile")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          logger.info(s"count = $count, status = $status")
          FilePart(partName, filename, contentType, path.toFile)
      }
  }

  /**
    * A generic operation on the temporary file that deletes the temp file after completion.
    */
  private def parseOnTempFile(file: File):Future[Seq[String]] = {
    //Source[String, Future[IOResult]]

    FileIO.fromPath(file.toPath)
      .via(Framing.delimiter(ByteString(System.lineSeparator), maximumFrameLength = 512, allowTruncation = true))
      .map(_.utf8String)
      .runWith(Sink.seq[String])
  }


  /**
    * A generic operation on the temporary file that deletes the temp file after completion.
    */
  private def operateOnTempFile(file: File) = {
    val size = Files.size(file.toPath)
    logger.info(s"size = ${size}")
    Files.deleteIfExists(file.toPath)
    size
  }

  /**
    * Uploads a multipart file as a POST request.
    *
    * @return
    */
  def uploadLog = Action.async(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file, fileSize, dispositionType) =>
        logger.info(s"key = $key, filename = $filename, contentType = $contentType, file = $file, fileSize = $fileSize, dispositionType = $dispositionType")
        file
    }
    fileOption match {
      case Some(f) => parseOnTempFile(f).map{str =>
        val data = operateOnTempFile(f)
        Ok(s"${str.mkString("\n")}")
      }
      case None => Future.successful(Ok(s"No file to process"))
    }
  }

}
