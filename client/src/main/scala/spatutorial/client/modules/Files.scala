package spatutorial.client.modules

import diode.data.Pot
import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.SPAMain.{Loc, TodoLoc}
import spatutorial.client.components.Bootstrap._
import spatutorial.client.components._
import spatutorial.client.services.FileListModel
import spatutorial.shared.FileListItem

import scalacss.ScalaCssReact._

import scala.util.Random
import scala.language.existentials


object Files {
  
  type ProxyType = Pot[FileListModel[FileListItem]]

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[ProxyType])

  case class State(selectedItem: Option[FileListItem] = None, showItemForm: Boolean = false)
  
  class Backend(bes: BackendScope[Props, State]) {
    import spatutorial.client.services.FileListHandler.Actions
    
    def mounted(props: Props) = {
      // dispatch a message to refresh the list, which will cause ListStore to fetch todos from the server
      Callback.when(props.proxy().isEmpty)(props.proxy.dispatchCB(Actions.RefreshFileList("")))
    }
    
    def newListItem() = {
      //activate the upload dialog
      bes.modState(s => s.copy(showItemForm = true))
    }
    
//    def editListItem(item: Option[FileListItem]) = {
//      //activate the edit dialog
//      bes.modState(s => s.copy(selectedItem = item, showItemForm = true))
//    }
    
    def itemEdited(item: FileListItem, cancelled: Boolean) = {
      val cb = 
        if (cancelled) {
          Callback.log("Item editing cancelled")
        }
        else {
          Callback.log(s"Item edited ${item.filename}") // >> bes.props >>= (_.proxy.dispatchCB(Actions.UpdateTodo(item)))
        }
      
      // hide the edit dialog, chain callbacks
      cb >> bes.modState(s => s.copy(showItemForm = false))
    }
    
    def render(p: Props, s: State) = {
      Panel(
          Panel.Props("Available Files"),
          <.div(
              p.proxy().renderFailed(ex => "Error loading"),
              p.proxy().renderPending(_ > 500, _ => "Loading..."),
              p.proxy().render{ fileList => 
                FileList(
                    fileList.fileItems,
//                    item => p.proxy.dispatchCB(Actions.UpdateTodo(item)),
//                    item => editListItem(Some(item)),
                    item => p.proxy.dispatchCB(Actions.DeleteFile(item))
                    )
              },
              Button(Button.Props(newListItem()), Icon.plusSquare, " New"),
              
              if (s.showItemForm)     // if the dialog is open, add it to the panel
                FileUploadForm(FileUploadForm.Props(s.selectedItem, itemEdited))
              else                    // otherwise add an empty placeholder
                VdomArray.empty()
              )
          )
    }
  }

  // create the React component for ItemList
  val component = ScalaComponent.builder[Props]("Files")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build
  

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[ProxyType]) = component(Props(router, proxy))
}




object FileUploadForm {
  type ItemType = FileListItem
  
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(item: Option[ItemType], submitHandler: (ItemType, Boolean) => Callback)

  case class State(item: ItemType, cancelled: Boolean = true)

  class Backend(t: BackendScope[Props, State]) {
    def submitForm(): Callback = {
      // mark it as NOT cancelled (which is the default)
      t.modState(s => s.copy(cancelled = false))
    }

    def formClosed(state: State, props: Props): Callback =
      // call parent handler with the new item and whether form was OK or cancelled
      props.submitHandler(state.item, state.cancelled)

      /*
       * Useful resource for Html tags/attributes
       * https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/file
       * https://developer.mozilla.org/en-US/docs/Web/API/FileList
       * 
       * Definition of ReactEventFromInput
       * https://github.com/japgolly/scalajs-react/blob/master/core/src/main/scala/japgolly/scalajs/react/events.scala
       * encapsulates type html.Input
       * https://github.com/scala-js/scala-js-dom/blob/master/src/main/scala/org/scalajs/dom/html.scala
       * HtmlInputElement
       * https://github.com/scala-js/scala-js-dom/blob/deffe6cc956afd41b7572d0f5b386c4c1b63534b/src/main/scala/org/scalajs/dom/raw/Html.scala
       * line 2395
       * list of members for an InputElement
       * 
       * defs for FileList and File
       * https://github.com/scala-js/scala-js-dom/blob/159526a5f06a6675df42879b259e417c4b38331e/src/main/scala/org/scalajs/dom/raw/lib.scala
       * File: line 7330
       * Blob: line 7960
       */
    def updateDescription(e: ReactEventFromInput) = {
//      import japgolly.scalajs.react.extra.Ajax
//      val ajax = Ajax("POST", "/upload")
//      .setRequestHeader("Content-Type", "multipart/form-data")
//      .send(e.target.files(0))
      
      
      println("just attempted send")
      println("In update Description")
      println("n files: " + e.target.files.length)
      val files = e.target.files
      val text = e.target.value
      // update ItemType content
      t.modState(s => s.copy(item = s.item.copy(filename = text)))
    }

    def updatePriority(e: ReactEventFromInput) = {
//      // update ItemType priority
//      val newPri = e.currentTarget.value match {
//        case p if p == TodoHigh.toString => TodoHigh
//        case p if p == TodoNormal.toString => TodoNormal
//        case p if p == TodoLow.toString => TodoLow
//      }
//      t.modState(s => s.copy(item = s.item.copy(priority = newPri)))
    }

    def onClick(e: ReactEventFromInput) = {
      println("on Click")
//      // update ItemType priority
//      val newPri = e.currentTarget.value match {
//        case p if p == TodoHigh.toString => TodoHigh
//        case p if p == TodoNormal.toString => TodoNormal
//        case p if p == TodoLow.toString => TodoLow
//      }
//      t.modState(s => s.copy(item = s.item.copy(priority = newPri)))
      t.modState(s => s.copy())
    }

    def render(p: Props, s: State) = {
      println("In file input form render")
//      log.debug(s"User is ${if (s.item.id == "") "adding" else "editing"} a todo or two")
      val headerText = "Select file"
      Modal(Modal.Props(
        // header contains a cancel button (X)
        header = hide => <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> hide, Icon.close), <.h4(headerText)),
        // footer has the OK button that submits the form before hiding it
        footer = hide => <.span(Button(Button.Props(submitForm() >> hide), "OK")),
        // this is called after the modal has been hidden (animation is completed)
        closed = formClosed(s, p)),
        <.div(bss.formGroup,
            <.label(^.`for` := "description", "Description0"),
            <.input.file(
                bss.formControl, 
                ^.id := "description", 
//                ^.value := s.item.filename,
                ^.placeholder := "write description", 
                ^.onChange ==> updateDescription,
                ^.onClick ==> onClick)
            )  //,
//        <.div(bss.formGroup,
//            <.label(^.`for` := "description", "Description0"),
//            <.input.text(
//                bss.formControl, 
//                ^.id := "description", 
//                ^.value := s.item.filename,
//                ^.placeholder := "write description", 
//                ^.onChange ==> updateDescription,
//                ^.onClick ==> onClick)
//            )  //,
            
//        <.div(bss.formGroup,
//          <.label(^.`for` := "priority", "Priority"),
//          // using defaultValue = "Normal" instead of option/selected due to React
//          <.select(bss.formControl, ^.id := "priority", ^.value := s.item.priority.toString, ^.onChange ==> updatePriority,
//            <.option(^.value := TodoHigh.toString, "High"),
//            <.option(^.value := TodoNormal.toString, "Normal"),
//            <.option(^.value := TodoLow.toString, "Low")
//          )
//        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("FileUpload Form")
    .initialStateFromProps(p => State(p.item.getOrElse(FileListItem("", ""))))
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}















