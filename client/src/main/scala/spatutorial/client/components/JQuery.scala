package spatutorial.client.components

import org.scalajs.dom._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Minimal facade for JQuery. Use https://github.com/scala-js/scala-js-jquery or
  * https://github.com/jducoeur/jquery-facade for more complete one.
  */
@js.native
trait JQueryEventObject extends Event {
  var data: js.Any = js.native
}

@js.native
@JSImport("jquery", JSImport.Default)
object JQueryStatic extends js.Object {
  def apply(element: Element): JQuery = js.native
}

@js.native
trait JQuery extends js.Object {
  def on(events: String, selector: js.Any, data: js.Any, handler: js.Function1[JQueryEventObject, js.Any]): JQuery = js.native
  def off(events: String): JQuery = js.native
}