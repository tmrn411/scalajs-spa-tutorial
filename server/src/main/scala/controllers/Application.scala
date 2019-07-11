package controllers

import scala.concurrent.{ExecutionContext, Future}

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.nio.ByteBuffer
import javax.inject._

import akka.stream.IOResult

import boopickle.Default._

import play.api.{Configuration, Environment}
import play.api._ 
import play.api.libs.streams.Accumulator
import play.api.mvc._

import services.ApiService
import services.FileListService
import spatutorial.shared.Api
import spatutorial.shared.FileListApi

object Router extends autowire.Server[ByteBuffer, Pickler, Pickler] {
  override def read[R: Pickler](p: ByteBuffer) = Unpickle[R].fromBytes(p)
  override def write[R: Pickler](r: R) = Pickle.intoBytes(r)
}

@Singleton
class Application @Inject()(cc:MessagesControllerComponents)(
    implicit 
    val config: Configuration, 
    env: Environment,
    executionContext: ExecutionContext
    ) extends MessagesAbstractController(cc) {
  
  val apiService = new ApiService()
  
  val fileListService = new FileListService()
  
  private val logger = Logger(this.getClass)

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
}
