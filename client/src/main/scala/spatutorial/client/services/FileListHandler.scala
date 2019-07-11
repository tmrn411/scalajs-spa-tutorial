package spatutorial.client.services

import autowire._
import diode._
import diode.data._
import diode.util._
import diode.react.ReactConnector
import boopickle.Default._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue


import spatutorial.shared.FileListApi
import spatutorial.shared.FileListItem

/**
  * Handles actions related to list of FileListItems
  *
  * @param modelRW Reader/Writer to access the model
  */
class FileListHandler[M](modelRW: ModelRW[M, Pot[FileListModel[FileListItem]]]) extends ActionHandler(modelRW) {
  
  import FileListHandler.Actions._
  
  override def handle = {
    case RefreshFileList(relDir) =>
      effectOnly(Effect(AjaxClient[FileListApi].getFileList(relDir).call().map(UpdateFileList)))
      
    case UpdateFileList(files) =>
      // got new file list from server, update model
      updated(Ready(FileListModel(files)))

    case DeleteFile(fileItem) =>
      // make a local update and inform server
      updated(value.map(_.remove(fileItem)), Effect(AjaxClient[FileListApi].deleteFile(fileItem.relDir, fileItem.filename).call().map(UpdateFileList)))
  }
}

object FileListHandler {
  
  object Actions {
    
    case class RefreshFileList(relDir: String) extends Action
    
    case class UpdateFileList(fileItems: Seq[FileListItem]) extends Action
    
    case class DeleteFile(fileItem: FileListItem) extends Action
  }
}