import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.{Files, Paths}
import java.util

import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.simple.ISimpleInArchive
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem


object Extract {
  def time(action: =>Unit):Unit={
    val t1 = System.nanoTime

    action

    println((System.nanoTime - t1) / 1e6d)
  }

  def main(args: Array[String]): Unit = {
    time { // 7603.8941 ms
      if (args.length == 0) {
        System.out.println("Usage: java ExtractItemsSimple <archive-name>")
        return
      }
      var randomAccessFile: RandomAccessFile = null
      var inArchive: IInArchive = null
      try {
        randomAccessFile = new RandomAccessFile(args(0), "r")
        inArchive = SevenZip.openInArchive(null, // autodetect archive type
          new RandomAccessFileInStream(randomAccessFile))
        // Getting simple interface of the archive inArchive
        val simpleInArchive = inArchive.getSimpleInterface
        val dir = System.getProperty("user.dir")
        for (item <- simpleInArchive.getArchiveItems) {
          val path = Paths.get(dir, "tmp", item.getPath)
          if (!item.isFolder) {
            if(!path.getParent.toFile.exists()) path.getParent.toFile.mkdirs()
            path.toFile.createNewFile()
            val result = item.extractSlow(new ISequentialOutStream() {
              @throws[SevenZipException]
              override def write(data: Array[Byte]): Int = {
                Files.newOutputStream(path).write(data)
                data.length // Return amount of consumed data
              }
            })
          } else {
            path.toFile.mkdirs()
          }
        }
      } catch {
        case e: Exception =>
          System.err.println("Error occurs: " + e)
      } finally {
        if (inArchive != null) try inArchive.close()
        catch {
          case e: SevenZipException =>
            System.err.println("Error closing archive: " + e)
        }
        if (randomAccessFile != null) try randomAccessFile.close()
        catch {
          case e: IOException =>
            System.err.println("Error closing file: " + e)
        }
      }
    }
  }
}
