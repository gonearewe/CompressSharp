package fun.mactavish.compress_sharp

import java.io.File

sealed abstract class FileType(val fileType: String, val extensions: Set[String]) {
  val icon: File = new File(getClass.getResource(s"/$fileType.png").getFile)
}

case object AUDIO_FILE extends FileType("audio", Set("mp3", "flac"))

case object IMAGE_FILE extends FileType("img", Set("png", "svg", "jpg", "jpeg"))

case object TEXT_FILE extends FileType("txt", Set("txt", "md"))

case object VIDEO_FILE extends FileType("video", Set("mp4"))

case object ARCHIVE_FILE extends FileType("archive", Set("tar", "zip", "gz", "xz", "7z", "rar"))

case object DIRECTORY extends FileType("directory", Set())

case object UNKNOWN_FILE extends FileType("file", Set())

object FileType {
  def of(extension: String): FileType = extension match {
    case _ if AUDIO_FILE.extensions contains extension => AUDIO_FILE
    case _ if IMAGE_FILE.extensions contains extension => IMAGE_FILE
    case _ if TEXT_FILE.extensions contains extension => TEXT_FILE
    case _ if VIDEO_FILE.extensions contains extension => VIDEO_FILE
    case _ if ARCHIVE_FILE.extensions contains extension => ARCHIVE_FILE
    case _ => UNKNOWN_FILE
  }
}
