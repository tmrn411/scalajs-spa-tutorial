package services

import scala.collection.JavaConverters._

import java.util.{UUID, Date}
import java.nio.file.Files
import java.nio.file.Paths  

import spatutorial.shared._

class FileListService extends FileListApi {
  val rootDir = controllers.AppFileIO.rootDir

  
  // get list of files
  override def getFileList(relDir: String): Seq[FileListItem] = {
    val dirPath = Paths.get(rootDir, relDir)
    if (Files.exists(dirPath)) {
      val fileListItems = Files.newDirectoryStream(dirPath).iterator.asScala.map{ path =>
        println("dir path: " + path.toString)
        FileListItem(path.getFileName.toString, relDir)
      }.toSeq
      fileListItems
    }
    else {
      println("dir " + dirPath.toString + " is empty") 
      Seq.empty[FileListItem]
    }
  }


  // delete an file
  override def deleteFile(relDir: String, filename: String): Seq[FileListItem] = {
    println(s"Deleting file: $filename")
    val path = Paths.get(rootDir, relDir, filename)
    Files.deleteIfExists(path)
    getFileList(relDir)
  }
}
