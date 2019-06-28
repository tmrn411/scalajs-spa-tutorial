package spatutorial.client.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap.{CommonStyle, Button}
import spatutorial.shared._
import scalacss.ScalaCssReact._

object FileList {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(
    items: Seq[FileListItem],
//    stateChange: FileListItem => Callback,
//    editItem: FileListItem => Callback,
    deleteItem: FileListItem => Callback
  )

  private val component = ScalaComponent.builder[Props]("Files")
    .render_P(p => {
      val style = bss.listGroup
      def renderItem(item: FileListItem) = {
        // convert priority into Bootstrap style
        val itemStyle = style.item
        <.li(itemStyle,
          <.span(item.filename),
          Button(Button.Props(p.deleteItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS)), "Delete")
        )
      }
      <.ul(style.listGroup)(p.items toTagMod renderItem)
    })
    .build

  def apply(
      items: Seq[FileListItem], 
//      stateChange: FileListItem => Callback, 
//      editItem: FileListItem => Callback, 
      deleteItem: FileListItem => Callback
      ) = {
//    component(Props(items, stateChange, editItem, deleteItem))
    component(Props(items, deleteItem))
  }
}
