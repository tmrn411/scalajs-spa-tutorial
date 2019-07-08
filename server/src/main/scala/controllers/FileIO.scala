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

import play.api.{Configuration, Environment}
import play.api._ 
import play.api.libs.streams.Accumulator
import play.api.mvc._

@Singleton
class FileIO @Inject()(cc:MessagesControllerComponents)(
    implicit 
    val config: Configuration, 
    env: Environment,
    executionContext: ExecutionContext, 
    val mat: Materializer
    ) extends MessagesAbstractController(cc) {
  
  private val logger = Logger(this.getClass)
  
  private var testFilePath: Option[Path] = Some(Paths.get("/var/tmp/Classifier/tmp.mat"))
  
  private val rootDir = "/var/tmp/Classifier"
  
  def checkAndCreateDir( fullPath: Path ) {
    if ( Files.notExists(fullPath)) { 
      val dp = new File( fullPath.toString )
      if (!dp.mkdirs())
        throw new Exception("mkdirs failed: " + fullPath.getParent().toString )
    }
  }

  
  /*
   * ref on writing custom body parser
   * https://www.playframework.com/documentation/2.7.x/ScalaBodyParsers
   */
  private val binaryFileParser: BodyParser[IOResult] = BodyParser{ request =>
    val rootPath = Paths.get(rootDir)
    checkAndCreateDir( rootPath )
    val path: Path = Files.createTempFile( rootPath, "tmp", ".mat")
    println("will save to " + path.toString)
    val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
    val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
    accumulator.map(Right.apply)
  }
  
  
  def upload = Action(binaryFileParser) { implicit request =>
    println("Got into here")
    val fname = request.headers.get("X-FILENAME")
    val pname = request.headers.get("X-PATHNAME")
    println("got file with name: " + fname)
    println("got path with name: " + pname)
    
    Ok("")
  }
  
  /*
   * docs on serving files
   * https://www.playframework.com/documentation/2.0.1/ScalaStream
   * https://stackoverflow.com/questions/13917105/how-to-download-a-file-with-play-framework-2-0
   */
  def download = Action { 
    logger.info("downloading " + testFilePath.get.toString)
    Ok.sendFile(new java.io.File(testFilePath.get.toString))
  } 
}
