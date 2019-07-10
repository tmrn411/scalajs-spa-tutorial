package spatutorial.client.components

import java.nio.file.Paths

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap.{CommonStyle, Button, SubmitFormButton}
import spatutorial.shared._
import scalacss.ScalaCssReact._

object FileList {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(
    items: Seq[FileListItem],
    deleteItem: FileListItem => Callback
  )
  
  def validateRelPath(relPath: String): String = {
    val path = relPath.dropWhile( _ == '/' )
    if (path.isEmpty)
      "/"
    else
      path + { if (path.last != "/") "/" else "" }
  }

  /*
   * List all the file items.
   * 
   * In this example, each list item shows 3 different ways to implement the download
   *   1. the file name as a link
   *   2. a submit button wrapped in a form
   *   3. an anchor tag made to appear as a button (not sure this will render properly on all browsers)
   */
  private val component = ScalaComponent.builder[Props]("Files")
    .render_P(p => {
      val style = bss.listGroup
      def renderItem(item: FileListItem) = {
        val url = "/download" +  validateRelPath(item.relDir) + item.filename
        val itemStyle = style.item
        <.li(
            itemStyle,
            bss.clearFix,
            <.a(item.filename,  ^.href := url, Seq(bss.pullLeft).toTagMod),
            Button(Button.Props(p.deleteItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Delete"),
            <.a(bss.button, "Download LB",  ^.href := url, bss.pullRight, bss.buttonXS ),
            SubmitFormButton(SubmitFormButton.Props(url, addStyles = Seq(bss.pullRight, bss.buttonXS)), "Download FB")
        )
      }
      <.ul(style.listGroup)(p.items toTagMod renderItem)
    })
    .build

  def apply(
      items: Seq[FileListItem], 
      deleteItem: FileListItem => Callback
      ) = {
    component(Props(items, deleteItem))
  }
}
