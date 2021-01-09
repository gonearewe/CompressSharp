import java.io.IOException
import java.io.RandomAccessFile

import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.IOutCreateArchive
import net.sf.sevenzipjbinding.IOutCreateCallback
import net.sf.sevenzipjbinding.IOutFeatureSetLevel
import net.sf.sevenzipjbinding.IOutFeatureSetMultithreading
import net.sf.sevenzipjbinding.IOutItemAllFormats
import net.sf.sevenzipjbinding.ISequentialInStream
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.OutItemFactory
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import net.sf.sevenzipjbinding.util.ByteArrayStream
import java.io.File

import scala.util.Random

object CompressArchiveStructure {
  def create: Array[CompressArchiveStructure.Item] = { //     <root>
    //     |
    //     +- info.txt
    //     +- random-100-bytes.dump
    //     +- dir1
    //     |  +- file-in-a-directory1.txt
    //     +- dir2
    //        +- file-in-a-directory2.txt
    val items = new Array[CompressArchiveStructure.Item](5)
    items(0) = new CompressArchiveStructure.Item("info.txt", "This is the info")
    val content = new Array[Byte](100)
    new Random().nextBytes(content)
    items(1) = new CompressArchiveStructure.Item("random-100-bytes.dump", content)
    // dir1 doesn't have separate archive item
    items(2) = new CompressArchiveStructure.Item("dir1" + File.separator + "file1.txt", "This file located in a directory 'dir'")
    // dir2 does have separate archive item
    items(3) = new CompressArchiveStructure.Item("dir2" + File.separator, null.asInstanceOf[Array[Byte]])
    items(4) = new CompressArchiveStructure.Item("dir2" + File.separator + "file2.txt", "This file located in a directory 'dir'")
    items
  }

  class Item(var path: String, var content: Array[Byte]) {
    def this(path: String, content: String) {
      this(path, content.getBytes)
    }

    def getPath: String = path

    def getContent: Array[Byte] = content
  }

}

object CompressGeneric {
  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      System.out.println("Usage: java CompressGeneric " + "<archive-format> <archive> <count-of-files>")
      for (af <- ArchiveFormat.values) {
        if (af.isOutArchiveSupported) System.out.println("Supported formats: " + af.name)
      }
      return
    }
    val itemsCount = Integer.valueOf(args(2))
    new CompressGeneric().compress(args(0), args(1), itemsCount)
  }
}

class CompressGeneric {

  /**
   * The callback provides information about archive items.
   */
  final private class MyCreateCallback extends IOutCreateCallback[IOutItemAllFormats] {
    @throws[SevenZipException]
    override def setOperationResult(operationResultOk: Boolean): Unit = {
      println(operationResultOk)
    }

    @throws[SevenZipException]
    override def setTotal(total: Long): Unit = {
      println(s">$total>")
    }

    @throws[SevenZipException]
    override def setCompleted(complete: Long): Unit = {
      println(s"--$complete--")
    }

    override def getItemInformation(index: Int, outItemFactory: OutItemFactory[IOutItemAllFormats]): IOutItemAllFormats = {
      val item = outItemFactory.createOutItem
      if (items(index).getContent == null) { // Directory
        item.setPropertyIsDir(true)
      }
      else { // File
        item.setDataSize(items(index).getContent.length.asInstanceOf[Long])
      }
      item.setPropertyPath(items(index).getPath)
      item
    }

    @throws[SevenZipException]
    override def getStream(i: Int): ISequentialInStream = {
      if (items(i).getContent == null) return null
      new ByteArrayStream(items(i).getContent, true)
    }
  }

  private var items:Array[CompressArchiveStructure.Item] = null

  private def compress(fmtName: String, filename: String, count: Int): Unit = {
    items = CompressArchiveStructure.create
    var success = false
    var raf:RandomAccessFile = null
    var outArchive:IOutCreateArchive[IOutItemAllFormats] = null
    val archiveFormat = ArchiveFormat.valueOf(fmtName)
    try {
      raf = new RandomAccessFile(filename, "rw")
      // Open out-archive object
      outArchive = SevenZip.openOutArchive(archiveFormat)
      // Configure archive
      outArchive match {
        case level: IOutFeatureSetLevel => level.setLevel(5)
        case _ =>
      }
      outArchive match {
        case multithreading: IOutFeatureSetMultithreading => multithreading.setThreadCount(2)
        case _ =>
      }
      // Create archive
      outArchive.createArchive(new RandomAccessFileOutStream(raf), count, new MyCreateCallback())
      success = true
    } catch {
      case e: SevenZipException =>
        System.err.println("7z-Error occurs:")
        // Get more information using extended method
        e.printStackTraceExtended()
      case e: Exception =>
        System.err.println("Error occurs: " + e)
    } finally {
      if (outArchive != null) try outArchive.close()
      catch {
        case e: IOException =>
          System.err.println("Error closing archive: " + e)
          success = false
      }
      if (raf != null) try raf.close()
      catch {
        case e: IOException =>
          System.err.println("Error closing file: " + e)
          success = false
      }
    }
    if (success) System.out.println(archiveFormat.getMethodName + " archive with " + count + " item(s) created")
  }
}
