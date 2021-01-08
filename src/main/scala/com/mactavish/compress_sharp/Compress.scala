package com.mactavish.compress_sharp

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.io._

import org.apache.commons.compress.archivers._
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.apache.commons.compress.utils.IOUtils

object Compress {
  def main(args: Array[String]): Unit = {
    val targetDir: File = new File("E:\\MyProjects\\CompressSharp\\build\\output")
    val from = Files.newInputStream(Path.of("E:\\MyProjects\\CompressSharp\\build\\新建文件夹.zip"))
    val i = (new ArchiveStreamFactory).createArchiveInputStream(ArchiveStreamFactory.ZIP, from)

    while (true) {

      val entry = i.getNextEntry
      if (entry == null) return
      else {
        val name = Paths.get(targetDir.getPath, entry.getName).toFile.getAbsolutePath
        val f = new File(name)

        if (entry.isDirectory) {
          f.mkdir()
        } else {
          f.createNewFile()
          IOUtils.copy(i, Files.newOutputStream(f.toPath))
        }
      }
    }
  }
}

