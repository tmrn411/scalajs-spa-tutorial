package spatutorial.client.components

import spatutorial.client.CssSettings._

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  style(unsafeRoot("body")(
    paddingTop(70.px))
  )
  
  /*
   * this is a shim/hack to display a single buttom that opens a file chooser
   * https://stackoverflow.com/questions/11235206/twitter-bootstrap-form-file-element-upload-button#18164555
   * 
   * it implements this css
   * note: [] notation is attribute selector, so I think this should match any element tag that has hidden attribute
   * [hidden] {
   *   display: none !important;
   * }
   * 
   * scalacss pseudo selectors
   * https://japgolly.github.io/scalacss/book/features/cond.html
   * info on display property
   * https://developer.mozilla.org/en-US/docs/Web/CSS/display
   * 
   * This should not be required after migrating to Bootstrap 4
   */
  style(unsafeRoot("")(
      &.attrExists("hidden").not("important")(
          display.none
          )
  ))

  val bootstrapStyles = new BootstrapStyles
}
