package controllers

import scala.concurrent.{ExecutionContext, Future}

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.nio.ByteBuffer
import javax.inject._

import akka.stream.IOResult
import akka.stream.Materializer
/*
 * DO NOT import FileIO, Sink or other classes from akka.stream.scaladsl directly.
 * Use the ._ wild card.  Otherwise may get strange type mismatch errors related
 * to conversion of java CompletionStage to scala Future.  There is some
 * implicit magic happening when the ._ wildcard is used.
 */
import akka.stream.scaladsl._
import akka.util.ByteString

import boopickle.Default._

import play.api.{Configuration, Environment}
import play.api._ 
//import play.api.data.Form
//import play.api.data.Forms._
//import play.api.libs.streams._
import play.api.libs.streams.Accumulator
import play.api.mvc._
import play.api.mvc.MultipartFormData.FilePart
import play.core.parsers.Multipart.FileInfo

import services.ApiService
import services.FileListService
import spatutorial.shared.Api
import spatutorial.shared.FileListApi

import scala.concurrent.ExecutionContext.Implicits.global



case class FormData(name: String)

object Router extends autowire.Server[ByteBuffer, Pickler, Pickler] {
  override def read[R: Pickler](p: ByteBuffer) = Unpickle[R].fromBytes(p)
  override def write[R: Pickler](r: R) = Pickle.intoBytes(r)
}

@Singleton
class Application @Inject()(cc:MessagesControllerComponents)(
    implicit 
    val config: Configuration, 
    env: Environment,
    executionContext: ExecutionContext, 
    val mat: Materializer
    ) extends MessagesAbstractController(cc) {
  
  val apiService = new ApiService()
  
  val fileListService = new FileListService()
  
  private val logger = Logger(this.getClass)
  
  private var testFilePath: Option[Path] = None
  
  private val rootDir = "/var/tmp/Classifier"
  
  def checkAndCreateDir( fullPath: Path ) {
    if ( Files.notExists(fullPath)) { 
      val dp = new File( fullPath.toString )
      if (!dp.mkdirs())
        throw new Exception("mkdirs failed: " + fullPath.getParent().toString )
    }
  }

  def index = Action {
    logger.info("Starting SPA tutorial")
    Ok(views.html.index("SPA tutorial"))
  }

  def autowireApi(path: String) = Action.async(parse.raw) {
    implicit request =>
      println(s"Request path: $path")
      
      /*
       * Routes are partial functions, stack all the API routes together
       * to form route dispatcher
       */
      val router = 
        (Router.route[Api](apiService))
        .orElse(Router.route[FileListApi](fileListService))

      // get the request body as ByteString
      val b = request.body.asBytes(parse.UNLIMITED).get

      // call Autowire route
//      Router.route[Api](apiService)(
//        autowire.Core.Request(path.split("/"), Unpickle[Map[String, ByteBuffer]].fromBytes(b.asByteBuffer))
      router(
        autowire.Core.Request(path.split("/"), Unpickle[Map[String, ByteBuffer]].fromBytes(b.asByteBuffer))
      ).map(buffer => {
        val data = Array.ofDim[Byte](buffer.remaining())
        buffer.get(data)
        Ok(data)
      })
  }

  def logging = Action(parse.anyContent) {
    implicit request =>
      request.body.asJson.foreach { msg =>
        println(s"CLIENT - $msg")
      }
      Ok("")
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
//      val path: Path = Files.createTempFile("multipartBody", "tempFile")
      val rootPath = Paths.get(rootDir)
      checkAndCreateDir( rootPath )
      val path: Path = Files.createTempFile( rootPath, "tmp", ".mat")
      testFilePath = Some(path)
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
  private def operateOnTempFile(file: File) = {
    val size = Files.size(file.toPath)
    logger.info(s"size = ${size}")
//    Files.deleteIfExists(file.toPath)
    size
  }
  
  /*
   * Needed to specify maxLength after files sizs started to get larger.
   * 
   * Docs discuss body parsers and maxLength method
   * https://www.playframework.com/documentation/2.7.x/ScalaBodyParsers
   * https://stackoverflow.com/questions/11696728/play-2-0-set-maximum-post-size-for-anycontent
   * 
   * However, after adding this, started to get error since it could not resolve implicit for Materializer.
   * https://stackoverflow.com/questions/36004414/play-2-5-migration-error-custom-action-with-bodyparser-could-not-find-implicit
   * 
   */
  def upload = Action( parse.maxLength(10 * 1024 * 1024, parse.multipartFormData(handleFilePartAsFile)) ) { implicit request =>
    
    val fileOption = request.body match {
      case Right(body) => {
        body.file("name").map {
          case FilePart(key, filename, contentType, file, fileSize, dispositionType) =>
            logger.info(s"key = $key, filename = $filename, contentType = $contentType, file = $file, fileSize = $fileSize, dispositionType = $dispositionType")
            val data = operateOnTempFile(file)
            data
        }
      }
      case Left(_) => throw new Exception("exceeded max file size")
    }

    Ok(s"file size = ${fileOption.getOrElse("no file")}")
  }
}
