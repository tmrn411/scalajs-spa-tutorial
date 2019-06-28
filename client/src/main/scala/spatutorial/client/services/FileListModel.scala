package spatutorial.client.services

import spatutorial.shared.FileListItem

case class FileListModel[T <: FileListItem](fileItems: Seq[T]) {
  
//  def add(fileItem: T) = {
//    if (fileItems.find(_.filename == fileItem.filename).isDefined)
//      this
//    else
//      FileListModel(fileItems := fileItem)
//  }
  
  def remove(fileItem: T) = FileListModel(fileItems.filterNot(_ == fileItem))
}