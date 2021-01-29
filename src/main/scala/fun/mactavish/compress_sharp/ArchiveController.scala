package fun.mactavish.compress_sharp

import java.nio.file.Path

import fun.mactavish.sevenz4s.Implicits._
import fun.mactavish.sevenz4s.creator.ArchiveCreator7Z
import fun.mactavish.sevenz4s.extractor.ArchiveExtractor
import fun.mactavish.sevenz4s.updater._
import fun.mactavish.sevenz4s.{ExtractionEntry, SevenZ4S}
import net.sf.sevenzipjbinding.ArchiveFormat

import scala.collection.mutable

object ArchiveController {
  def listEntries(f: Path): Seq[ExtractionEntry] = {
    val res = mutable.ArrayBuffer[ExtractionEntry]()
    new ArchiveExtractor()
      .from(f)
      .foreach(res.append)
      .close()
    res.toSeq
  }

  def extractAll(f: Path, to: Path): Unit = {
    new ArchiveExtractor()
      .from(f)
      .extractTo(to)
      .close()
  }

  def extractEntries(f: Path, relPaths: Set[String], to: Path): Unit = {
    new ArchiveExtractor()
      .from(f)
      .foreach(e => if (relPaths.exists(p => e.path.startsWith(p))) e.extractTo(to))
      .close()
  }

  def deleteEntries(f: Path, relPaths: Set[String]): Boolean = {
    val archive = new ArchiveExtractor().from(f)
    val format = archive.archiveFormat
    archive.close()

    if (!format.isOutArchiveSupported)
      return false

    format match {
      case ArchiveFormat.ZIP =>
        new ArchiveUpdaterZip().from(f).removeWhere(e => relPaths.exists(p => e.path.startsWith(p)))
      case ArchiveFormat.TAR =>
        new ArchiveUpdaterTar().from(f).removeWhere(e => relPaths.exists(p => e.path.startsWith(p)))
      case ArchiveFormat.GZIP =>
        new ArchiveUpdaterGZip().from(f).removeWhere(e => relPaths.exists(p => e.path.startsWith(p)))
      case ArchiveFormat.BZIP2 =>
        if (relPaths.isEmpty)
          new ArchiveUpdaterBZip2().from(f).removeWhere(_ => true)
        else return false
      case ArchiveFormat.SEVEN_ZIP =>
        new ArchiveUpdater7Z().from(f).removeWhere(e => relPaths.exists(p => e.path.startsWith(p)))
      case _ => return false
    }

    true
  }

  def compress(f: Path, to: Path) = {
    val entries = SevenZ4S.get7ZEntriesFrom(f)
    new ArchiveCreator7Z()
      .towards(to)
      .compress(entries)
  }
}
