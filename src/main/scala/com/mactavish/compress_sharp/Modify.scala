import java.io.Closeable
import java.io.RandomAccessFile
import java.util

import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.IInStream
import net.sf.sevenzipjbinding.IOutCreateCallback
import net.sf.sevenzipjbinding.IOutItemAllFormats
import net.sf.sevenzipjbinding.IOutUpdateArchive
import net.sf.sevenzipjbinding.ISequentialInStream
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.OutItemFactory
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import net.sf.sevenzipjbinding.util.ByteArrayStream

import scala.util.control.Breaks.{break, breakable}


object UpdateAddRemoveItems {
  def main(args: Array[String]): Unit = {
    if (args.length == 2) {
      new UpdateAddRemoveItems().compress(args(0), args(1))
      return
    }
    System.out.println("Usage: java UpdateAddRemoveItems <in> <out>")
  }
}

class UpdateAddRemoveItems {

  /**
   * The callback defines the modification to be made.
   */
  final private class MyCreateCallback extends IOutCreateCallback[IOutItemAllFormats] {
    @throws[SevenZipException]
    override def setOperationResult(operationResultOk: Boolean): Unit = {
      // Track each operation result here
    }

    @throws[SevenZipException]
    override def setTotal(total: Long): Unit = {
      // Track operation progress here
    }

    @throws[SevenZipException]
    override def setCompleted(complete: Long): Unit = {
    }

    @throws[SevenZipException]
    override def getItemInformation(index: Int, outItemFactory: OutItemFactory[IOutItemAllFormats]): IOutItemAllFormats = {
      if (index == itemToAdd) { // Adding new item
        val outItem = outItemFactory.createOutItem
        outItem.setPropertyPath(itemToAddPath)
        outItem.setDataSize(itemToAddContent.length.toLong)
        return outItem
      }
      // Remove item by changing the mapping "new index"->"old index"
      if (index < itemToRemove) return outItemFactory.createOutItem(index)
      outItemFactory.createOutItem(index + 1)
    }

    @throws[SevenZipException]
    override def getStream(i: Int): ISequentialInStream = {
      if (i != itemToAdd) return null
      new ByteArrayStream(itemToAddContent, true)
    }
  }

  var itemToAdd = 0 // New index of the item to add

  var itemToAddPath: String = null
  var itemToAddContent: Array[Byte] = null
  var itemToRemove = 0 // Old index of the item to be removed

  @throws[SevenZipException]
  private def initUpdate(inArchive: IInArchive): Unit = {
    itemToAdd = inArchive.getNumberOfItems - 1
    itemToAddPath = "data.dmp"
    itemToAddContent = "dmp-content".getBytes
    itemToRemove = -1
    breakable {
      for (i <- 0 until inArchive.getNumberOfItems) {
        if (inArchive.getProperty(i, PropID.PATH) == "info.txt") {
          itemToRemove = i
          break //todo: break is not supported
        }
      }
    }
    if (itemToRemove == -1) throw new RuntimeException("Item 'info.txt' not found")
  }

  private def compress(in: String, out: String): Unit = {
    var success = false
    var inRaf:RandomAccessFile = null
    var outRaf:RandomAccessFile = null
    var inArchive:IInArchive = null
    var outArchive:IOutUpdateArchive[IOutItemAllFormats] = null
    val closeables = new util.ArrayList[Closeable]
    try { // Open input file
      inRaf = new RandomAccessFile(in, "r")
      closeables.add(inRaf)
      val inStream = new RandomAccessFileInStream(inRaf)
      // Open in-archive
      inArchive = SevenZip.openInArchive(null, inStream)
      closeables.add(inArchive)
      initUpdate(inArchive)
      outRaf = new RandomAccessFile(out, "rw")
      closeables.add(outRaf)
      // Open out-archive object
      outArchive = inArchive.getConnectedOutArchive
      // Modify archive
      outArchive.updateItems(new RandomAccessFileOutStream(outRaf), inArchive.getNumberOfItems, new MyCreateCallback)
      success = true
    } catch {
      case e: SevenZipException =>
        System.err.println("7z-Error occurs:")
        // Get more information using extended method
        e.printStackTraceExtended()
      case e: Exception =>
        System.err.println("Error occurs: " + e)
    } finally for (i <- closeables.size - 1 to 0 by -1) {
      try closeables.get(i).close()
      catch {
        case e: Throwable =>
          System.err.println("Error closing resource: " + e)
          success = false
      }
    }
    if (success) System.out.println("Update successful")
  }
}
