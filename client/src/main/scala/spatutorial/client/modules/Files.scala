package spatutorial.client.modules

import org.scalajs.dom
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
import spatutorial.client.services.FileListHandler.Actions
import spatutorial.shared.FileListItem

import scalacss.ScalaCssReact._

import scala.util.Random
import scala.language.existentials


object Files {
  
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles
  
  type ProxyType = Pot[FileListModel[FileListItem]]

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[ProxyType])

  case class State(selectedItem: Option[FileListItem] = None, showItemForm: Boolean = false)
  
  class Backend(bes: BackendScope[Props, State]) {
    
    /*
     * resource for XMLHttpRequest
     * https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/Using_XMLHttpRequest#Handling_binary_data
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers
     * 
     * ajax upload
     * https://thoughtbot.com/blog/html5-powered-ajax-file-uploads
     * https://github.com/hussachai/play-scalajs-showcase/blob/master/example-client/src/main/scala/example/FileUploadJS.scala
     * 
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
    def uploadFile(file: dom.File, props: Props) = {
      val xhr = new dom.XMLHttpRequest
      if(xhr.upload != null) { // && file.size <= maxFileSize){
        
        xhr.onreadystatechange = (e: dom.Event) => {
          if(xhr.readyState == dom.XMLHttpRequest.DONE){
            if (xhr.status == 200)
              println("upload complete: success")
            else
              println("upload complete: error")
            
            props.proxy.dispatchNow(Actions.RefreshFileList(""))
//            val statusClass = if(xhr.status == 200) "label-success" else "label-danger"
//            val statusMsg = if(xhr.status == 200) "Success" else "Error "+xhr.statusText
//            $("#status").removeClass("hide").addClass(statusClass).text(statusMsg)
          }
        }
        
        xhr.open("POST", "/upload", true)
        xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest")
        xhr.setRequestHeader("X-FILENAME", file.name)
        xhr.setRequestHeader("X-PATHNAME", "myPath")
        xhr.send(file)
      }
    }
    
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
    def onFileSelectedCB(props: Props): ReactEventFromInput => Callback = {
      
      { e: ReactEventFromInput => 
        uploadFile(e.target.files(0), props)
        Callback.empty
      }
    }
    
    def mounted(props: Props) = {
      // dispatch a message to refresh the list, which will cause ListStore to fetch todos from the server
      Callback.when(props.proxy().isEmpty)(props.proxy.dispatchCB(Actions.RefreshFileList("")))
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
                    item => p.proxy.dispatchCB(Actions.DeleteFile(item))
                    )
              },
              FileChooserButton(FileChooserButton.Props(onFileSelectedCB(p), addStyles = Seq(bss.pullLeft)), Icon.plusSquare, " Upload")
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

















