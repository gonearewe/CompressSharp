package fun.mactavish.compress_sharp

import java.nio.file.Path

import fun.mactavish.sevenz4s.Implicits._
import fun.mactavish.sevenz4s.extractor.ArchiveExtractor
import fun.mactavish.sevenz4s.{CreatableArchiveFormat, ExtractionEntry, SevenZ4S}

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
    SevenZ4S.extract(f, to)
  }

  def extractEntries(f: Path, relPaths: Set[String], to: Path): Unit = {
    new ArchiveExtractor()
      .from(f)
      .foreach(e => if (relPaths.exists(p => e.path.startsWith(p))) e.extractTo(to))
      .close()
  }

  def compress(format: CreatableArchiveFormat, f: Path, to: Path) = {
    SevenZ4S.compress(format, f, to)
  }
}
