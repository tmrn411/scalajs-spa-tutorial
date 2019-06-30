package spatutorial.client.logger

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.annotation.JSImport.Namespace
import scala.scalajs.js.annotation.JSName

/**
 * Facade for functions in log4javascript that we need
 * 
 * Docs for log4javascript api
 * http://log4javascript.org/docs/manual.html#levels
 * 
 * Note: many api calls have optional arguments.  This facade does NOT
 * implement the full API.  Therefore, contructors and functions below
 * may have only a subset (ie. including 0) of the arguments available
 * in the underlying api.
 */
@JSImport("log4javascript", Namespace)
@js.native
private[logger] object Log4JavaScript extends js.Object {
  
  object Level extends Level
  
  def resetConfiguration(): Unit = js.native
  def getLogger(name:js.UndefOr[String]): JSLogger = js.native
  def setEnabled(enabled: Boolean):Unit = js.native
  def isEnabled: Boolean = js.native
  
  class BrowserConsoleAppender() extends Appender {}
  
  class PopUpAppender() extends Appender {}
  
  class AjaxAppender(url: String, withCredentials: js.UndefOr[Boolean] = js.undefined) extends Appender {
    def addHeader(name: String, value: String): Unit = js.native 
  }
  
  class JsonLayout() extends Layout {}
}

@js.native
private[logger] trait Level extends js.Object {
  val ALL:Level = js.native
  val TRACE:Level = js.native
  val DEBUG:Level = js.native
  val INFO:Level = js.native
  val WARN:Level = js.native
  val ERROR:Level = js.native
  val FATAL:Level = js.native
}

@js.native
private[logger] trait JSLogger extends js.Object {
  def addAppender(appender:Appender):Unit = js.native
  def removeAppender(appender:Appender):Unit = js.native
  def removeAllAppenders(appender:Appender):Unit = js.native
  def setLevel(level:Level):Unit = js.native
  def getLevel:Level = js.native
  def trace(msg:String, error:js.UndefOr[js.Error]):Unit = js.native
  def debug(msg:String, error:js.UndefOr[js.Error]):Unit = js.native
  def info(msg:String, error:js.UndefOr[js.Error]):Unit = js.native
  def warn(msg:String, error:js.UndefOr[js.Error]):Unit = js.native
  def error(msg:String, error:js.UndefOr[js.Error]):Unit = js.native
  def fatal(msg:String, error:js.UndefOr[js.Error]):Unit = js.native
  def trace(msg:String):Unit = js.native
  def debug(msg:String):Unit = js.native
  def info(msg:String):Unit = js.native
  def warn(msg:String):Unit = js.native
  def error(msg:String):Unit = js.native
  def fatal(msg:String):Unit = js.native
}

@js.native
private[logger] trait Layout extends js.Object

@js.native
private[logger] trait Appender extends js.Object {
  def setLayout(layout:Layout):Unit = js.native
  def setThreshold(level:Level):Unit = js.native
}


class L4JSLogger(jsLogger:JSLogger) extends Logger {
  import Log4JavaScript.AjaxAppender
  import Log4JavaScript.JsonLayout

  private var ajaxAppender: AjaxAppender = null

  private def undefOrError(e:Exception):js.UndefOr[js.Error] = {
    if(e == null)
      js.undefined
    else
      e.asInstanceOf[js.Error]
  }

  override def trace(msg: String, e: Exception): Unit = jsLogger.trace(msg, undefOrError(e))
  override def trace(msg: String): Unit = jsLogger.trace(msg)
  override def debug(msg: String, e: Exception): Unit = jsLogger.debug(msg, undefOrError(e))
  override def debug(msg: String): Unit = jsLogger.debug(msg)
  override def info(msg: String, e: Exception): Unit = jsLogger.info(msg, undefOrError(e))
  override def info(msg: String): Unit = jsLogger.info(msg)
  override def warn(msg: String, e: Exception): Unit = jsLogger.warn(msg, undefOrError(e))
  override def warn(msg: String): Unit = jsLogger.warn(msg)
  override def error(msg: String, e: Exception): Unit = jsLogger.error(msg, undefOrError(e))
  override def error(msg: String): Unit = jsLogger.error(msg)
  override def fatal(msg: String, e: Exception): Unit = jsLogger.fatal(msg, undefOrError(e))
  override def fatal(msg: String): Unit = jsLogger.fatal(msg)

  override def enableServerLogging(url: String): Unit = {
    if(ajaxAppender == null) {
      ajaxAppender = new AjaxAppender(url)
      ajaxAppender.addHeader("Content-Type", "application/json")
      ajaxAppender.setLayout(new JsonLayout())
      jsLogger.addAppender(ajaxAppender)

    }
  }

  override def disableServerLogging():Unit = {
    if(ajaxAppender != null) {
      jsLogger.removeAppender(ajaxAppender)
      ajaxAppender = null
    }
  }
}
