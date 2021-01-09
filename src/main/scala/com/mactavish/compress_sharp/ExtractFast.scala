import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.{Files, Paths}
import java.util

import net.sf.sevenzipjbinding.ExtractAskMode
import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.IArchiveExtractCallback
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream


object ExtractFast {

  class MyExtractCallback(var inArchive: IInArchive) extends IArchiveExtractCallback {
    private var hash = 0
    private var size = 0
    private var index = 0
    private var skipExtraction = false

    @throws[SevenZipException]
    override def getStream(index: Int, extractAskMode: ExtractAskMode): ISequentialOutStream = {
      this.index = index
      skipExtraction = inArchive.getProperty(index, PropID.IS_FOLDER).asInstanceOf[Boolean]
      if (skipExtraction || (extractAskMode ne ExtractAskMode.EXTRACT)) return null
      new ISequentialOutStream() {
        @throws[SevenZipException]
        override def write(data: Array[Byte]): Int = {
          val dir = System.getProperty("user.dir")
          val path = Paths.get(dir, "tmp", inArchive.getProperty(index, PropID.PATH).asInstanceOf[String])
          if (!inArchive.getProperty(index, PropID.IS_FOLDER).asInstanceOf[Boolean]) {
            if (!path.getParent.toFile.exists()) path.getParent.toFile.mkdirs()
            path.toFile.createNewFile()
            Files.newOutputStream(path).write(data)
          } else {
            path.toFile.mkdirs()
          }
          data.length // Return amount of consumed data
        }

      }
    }

    override def prepareOperation(extractAskMode: ExtractAskMode): Unit = {}

    override def setOperationResult(extractOperationResult: ExtractOperationResult): Unit = {}

    override def setTotal(l: Long): Unit = {}

    override def setCompleted(l: Long): Unit = {}
  }

  def time(action: => Unit): Unit = {
    val t1 = System.nanoTime

    action

    println((System.nanoTime - t1) / 1e6d)
  }

  def main(args: Array[String]): Unit = {
    time { // 5894.3657 ms
      if (args.length == 0) {
        System.out.println("Usage: java ExtractItemsStandard <arch-name>")
        return
      }
      var randomAccessFile: RandomAccessFile = null
      var inArchive: IInArchive = null
      try {
        randomAccessFile = new RandomAccessFile(args(0), "r")
        inArchive = SevenZip.openInArchive(null, // autodetect archive type
          new RandomAccessFileInStream(randomAccessFile))
        val in = new Array[Int](inArchive.getNumberOfItems)
        for (i <- in.indices) {
          in(i) = i
        }
        inArchive.extract(in, false, // Non-test mode
          new ExtractItemsStandardCallback.MyExtractCallback(inArchive))
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


