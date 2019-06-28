package spatutorial.shared

trait FileListApi {

  // get list of files
  def getFileList(relDir: String): Seq[FileListItem]

  // delete a file
  def deleteFile(relDir: String, filename: String): Seq[FileListItem]
}