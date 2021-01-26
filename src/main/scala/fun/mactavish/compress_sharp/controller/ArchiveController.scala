package fun.mactavish.compress_sharp.controller

import java.nio.file.Path

import fun.mactavish.sevenz4s.Implicits._
import fun.mactavish.sevenz4s.creator.ArchiveCreator7Z
import fun.mactavish.sevenz4s.extractor.ArchiveExtractor
import fun.mactavish.sevenz4s.{ExtractionEntry, SevenZ4S}

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

  def extractEntries(f: Path, entries: Set[ExtractionEntry], to: Path): Unit = {
    new ArchiveExtractor()
      .from(f)
      .foreach(e => if (entries contains e) e.extractTo(to))
      .close()
  }

  def compress(f: Path, to: Path) = {
    val entries = SevenZ4S.get7ZEntriesFrom(f)
    new ArchiveCreator7Z()
      .towards(to)
      .compress(entries)
  }
}
