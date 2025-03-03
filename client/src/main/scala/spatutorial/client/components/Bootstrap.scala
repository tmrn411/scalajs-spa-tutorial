package spatutorial.client.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.annotation.JSImport.Namespace
import scalacss.ScalaCssReact._
import spatutorial.client.CssSettings._

/**
 * Common Bootstrap components for scalajs-react
 */
object Bootstrap {

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles
  
  /*
   * After switching to npm modules (ie. instead of webjars) the Bootstrap lib 
   * must be explicitly loaded before the implicit jq2bootstrap is called 
   */
  private object BootstrapLib {
    @js.native
    @JSImport("bootstrap", Namespace)
    object BootstrapModule extends js.Object
    
    private lazy val dummy = BootstrapModule
    
    def load() = dummy
  }
  BootstrapLib.load()

  @js.native
  trait BootstrapJQuery extends JQuery {
    def modal(action: String): BootstrapJQuery = js.native
    def modal(options: js.Any): BootstrapJQuery = js.native
  }

  implicit def jq2bootstrap(jq: JQuery): BootstrapJQuery = jq.asInstanceOf[BootstrapJQuery]

  // Common Bootstrap contextual styles
  object CommonStyle extends Enumeration {
    val default, primary, success, info, warning, danger = Value
  }

  object Button {

    case class Props(onClick: Callback, style: CommonStyle.Value = CommonStyle.default, addStyles: Seq[StyleA] = Seq())

    val component = ScalaComponent.builder[Props]("Button")
      .renderPC((_, p, c) =>
        <.button(bss.buttonOpt(p.style), p.addStyles.toTagMod, ^.tpe := "button", ^.onClick --> p.onClick, c)
      ).build

    def apply(props: Props, children: VdomNode*) = component(props)(children: _*)
    def apply() = component
  }

  object FileChooserButton {

    case class Props(onChange: ReactEventFromInput => Callback, style: CommonStyle.Value = CommonStyle.default, addStyles: Seq[StyleA] = Seq())

    /*
     * see following on how to create single button file chooser:
     * https://stackoverflow.com/questions/11235206/twitter-bootstrap-form-file-element-upload-button#18164555
     * note: this also requred css, see entry for 'hidden' selector in GlobalStyles.scala
     */
    val component = ScalaComponent.builder[Props]("Button")
      .renderPC((_, p, c) =>
        <.label(
            //^.classSet1("btn btn-default"), // should be equivalent to next line when p.style = CommonStyle.default
            bss.buttonOpt(p.style),
            ^.onChange ==> p.onChange,
            c,
            <.input( ^.tpe := "file", ^.hidden := true)
            )
      ).build

    def apply(props: Props, children: VdomNode*) = component(props)(children: _*)
    def apply() = component
  }
  
  /*
   * A button that will trigger the submit behavior of its parent form
   * 
   * docs on bootstrap button tags
   * https://getbootstrap.com/docs/4.0/components/buttons/#button-tags
   */
  object SubmitButton {

    case class Props(onClick: Callback = Callback.empty, style: CommonStyle.Value = CommonStyle.default, addStyles: Seq[StyleA] = Seq())
    
    /*
     * On Chrome browser, the button maintains focus after being clicked.  This removes focus immediately after
     * the click.
     */
    def onMouseUp(e: ReactEventFromInput) = {
      Callback {
        println("on Mouseup")
        e.target.blur()
      }
    }

    val component = ScalaComponent.builder[Props]("Button")
      .renderPC((_, p, c) =>
        <.button(bss.buttonOpt(p.style), p.addStyles.toTagMod, ^.tpe := "submit", ^.onClick --> p.onClick, ^.onMouseUp ==> onMouseUp, c)
      ).build

    def apply(props: Props, children: VdomNode*) = component(props)(children: _*)
    def apply() = component
  }
  
  /*
   * A button pre-packaged as a form so that clicking the button will get the url.
   * Typical usage is to use this button to trigger file download
   * 
   * docs on bootstrap button tags
   * https://getbootstrap.com/docs/4.0/components/buttons/#button-tags
   */
  object SubmitFormButton {

    case class Props(url: String, onClick: Callback = Callback.empty, style: CommonStyle.Value = CommonStyle.default, addStyles: Seq[StyleA] = Seq()) //onClick: Callback, style: CommonStyle.Value = CommonStyle.default, addStyles: Seq[StyleA] = Seq())

    val component = ScalaComponent.builder[Props]("FormButton")
      .renderPC((_, p, c) =>
        <.form(
            ^.action := p.url,
            ^.method := "GET",
            SubmitButton(SubmitButton.Props(onClick = p.onClick, style = p.style, addStyles = p.addStyles), c)
            )
      ).build

    def apply(props: Props, children: VdomNode*) = component(props)(children: _*)
    def apply() = component
  }

  object Panel {

    case class Props(heading: String, style: CommonStyle.Value = CommonStyle.default)

    val component = ScalaComponent.builder[Props]("Panel")
      .renderPC((_, p, c) =>
        <.div(bss.panelOpt(p.style),
          <.div(bss.panelHeading, p.heading),
          <.div(bss.panelBody, c)
        )
      ).build

    def apply(props: Props, children: VdomNode*) = component(props)(children: _*)
    def apply() = component
  }

  object Modal {

    // header and footer are functions, so that they can get access to the the hide() function for their buttons
    case class Props(header: Callback => VdomNode, footer: Callback => VdomNode, closed: Callback, backdrop: Boolean = true,
                     keyboard: Boolean = true)

    class Backend(t: BackendScope[Props, Unit]) {
      def hide = {
        // instruct Bootstrap to hide the modal
        // Note, since React v16, pattern to get Dom node has changed, see .getDOMNode
        // https://github.com/japgolly/scalajs-react/blob/v1.3.1/doc/USAGE.md
        t.getDOMNode.map{_.toElement match {
          case Some(element) => jQuery(element).modal("hide")
          case _ => {}   // if node was unmounted (though this is not expected), just silently do nothing
        }}
        .void
      }

      // jQuery event handler to be fired when the modal has been hidden
      def hidden(e: JQueryEventObject): js.Any = {
        // inform the owner of the component that the modal was closed/hidden
        t.props.flatMap(_.closed).runNow()
      }

      def render(p: Props, c: PropsChildren) = {
        val modalStyle = bss.modal
        <.div(modalStyle.modal, modalStyle.fade, ^.role := "dialog", ^.aria.hidden := true,
          <.div(modalStyle.dialog,
            <.div(modalStyle.content,
              <.div(modalStyle.header, p.header(hide)),
              <.div(modalStyle.body, c),
              <.div(modalStyle.footer, p.footer(hide))
            )
          )
        )
      }
    }

    val component = ScalaComponent.builder[Props]("Modal")
      .renderBackendWithChildren[Backend]
      .componentDidMount(scope => Callback {
        // Note, since React v16, pattern to get Dom node has changed, see .getDOMNode
        // https://github.com/japgolly/scalajs-react/blob/v1.3.1/doc/USAGE.md
        val p = scope.props
        scope.getDOMNode.toElement match {
          case Some(element) => {
            // instruct Bootstrap to show the modal
            jQuery(element).modal(js.Dynamic.literal("backdrop" -> p.backdrop, "keyboard" -> p.keyboard, "show" -> true))
            // register event listener to be notified when the modal is closed
            jQuery(element).on("hidden.bs.modal", null, null, scope.backend.hidden _)
          }
          case _ => {}  // if node was unmounted (though this is not expected), just silently do nothing
        }
      })
      .build

    def apply(props: Props, children: VdomElement*) = component(props)(children: _*)
    def apply() = component
  }

}
