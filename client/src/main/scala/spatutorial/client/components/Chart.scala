package spatutorial.client.components

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSImport

@js.native
trait ChartDataset extends js.Object {
  def label: String = js.native
  def data: js.Array[Double] = js.native
  def fillColor: String = js.native
  def strokeColor: String = js.native
}

object ChartDataset {
  def apply(data: Seq[Double],
            label: String, backgroundColor: String = "#8080FF", borderColor: String = "#404080"): ChartDataset = {
    js.Dynamic.literal(
      label = label,
      data = data.toJSArray,
      backgroundColor = backgroundColor,
      borderColor = borderColor
    ).asInstanceOf[ChartDataset]
  }
}

@js.native
trait ChartData extends js.Object {
  def labels: js.Array[String] = js.native
  def datasets: js.Array[ChartDataset] = js.native
}

object ChartData {
  def apply(labels: Seq[String], datasets: Seq[ChartDataset]): ChartData = {
    js.Dynamic.literal(
      labels = labels.toJSArray,
      datasets = datasets.toJSArray
    ).asInstanceOf[ChartData]
  }
}

@js.native
trait ChartOptions extends js.Object {
  def responsive: Boolean = js.native
}

object ChartOptions {
  def apply(responsive: Boolean = true): ChartOptions = {
    js.Dynamic.literal(
      responsive = responsive
    ).asInstanceOf[ChartOptions]
  }
}

@js.native
trait ChartConfiguration extends js.Object {
  def `type`: String = js.native
  def data: ChartData = js.native
  def options: ChartOptions = js.native
}

object ChartConfiguration {
  def apply(`type`: String, data: ChartData, options: ChartOptions = ChartOptions(false)): ChartConfiguration = {
    js.Dynamic.literal(
      `type` = `type`,
      data = data,
      options = options
    ).asInstanceOf[ChartConfiguration]
  }
}

// define a class to access the Chart.js component
@js.native
@JSImport("chart.js", "Chart")
class JSChart(ctx: js.Dynamic, config: ChartConfiguration) extends js.Object

object Chart {

  // available chart styles
  sealed trait ChartStyle

  case object LineChart extends ChartStyle

  case object BarChart extends ChartStyle

  case class ChartProps(name: String, style: ChartStyle, data: ChartData, width: Int = 500, height: Int = 300)

  val Chart = ScalaComponent.builder[ChartProps]("Chart")
    .render_P(p =>
      <.canvas(VdomAttr("width") := p.width, VdomAttr("height") := p.height)
    )
    .componentDidMount(scope => Callback {
      /*
       * change from React v15 to 16.
       * https://github.com/japgolly/scalajs-react/blob/master/doc/USAGE.md
       */
      scope.getDOMNode.toElement match {
        case Some(element) => {
          // access context of the canvas
          val ctx = element.asInstanceOf[HTMLCanvasElement].getContext("2d")
          
          // create the actual chart using the 3rd party component
          scope.props.style match {
            case LineChart => new JSChart(ctx, ChartConfiguration("line", scope.props.data))
            case BarChart => new JSChart(ctx, ChartConfiguration("bar", scope.props.data))
            case _ => throw new IllegalArgumentException
          }
        }
        case _ => {}   // if node was unmounted (though this is not expected), just silently do nothing
      }
    }).build

  def apply(props: ChartProps) = Chart(props)
}
