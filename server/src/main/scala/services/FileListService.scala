package services

import java.util.{UUID, Date}

import spatutorial.shared._

class FileListService extends FileListApi {
  var files = Seq(
    FileListItem("file1.mat", "datasets"),
    FileListItem("file2.mat", "datasets")
  )

  
  // get list of files
  override def getFileList(relDir: String): Seq[FileListItem] = {
    // provide some fake items
    println(s"Sending ${files.size} file names")
    files
  }


  // delete an file
  override def deleteFile(relDir: String, filename: String): Seq[FileListItem] = {
    println(s"Deleting file: $filename")
    files = files.filterNot(_.filename == filename)
    files
  }
}
