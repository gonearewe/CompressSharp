package fun.mactavish.compress_sharp

import fun.mactavish.sevenz4s.{ExtractionEntry => Entry}

class Item(private val entry: Entry) {
  private var name: String = _

  def this(name: String) {
    this(null: Entry)
    this.name = name
  }

  def getName: String =
    if (entry != null) entry.path.replace("\\", "/").split("/").last
    else this.name

  def isDir: Boolean =
    if (entry != null) entry.isDir
    else true

  def getOriginalSize: String =
    if (entry != null) entry.originalSize.toString
    else ""

  def getPackedSize: String = {
    if (entry != null) entry.packedSize.toString
    else ""
  }

  def getCRC: String = {
    if (entry != null) entry.CRC.toString
    else ""
  }
}
