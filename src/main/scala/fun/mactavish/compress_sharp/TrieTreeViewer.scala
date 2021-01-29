package fun.mactavish.compress_sharp

import java.io.File

import fun.mactavish.sevenz4s.{ExtractionEntry => Entry}
import scalafx.collections.ObservableBuffer

import scala.collection.mutable

class TrieTreeViewer extends ObservableBuffer[Item] {
  private var path = mutable.Stack[String]()
  private var root: TrieTree[String, Item] = _
  private var cur: TrieTree[String, Item] = root

  def reset(entries: Seq[Entry]): Unit = {
    root = buildTrie(entries)
    cur = root
    path.clear()
    updateBuffer()
  }

  def pathOf(item: Item, sep: String = File.separator): String = {
    if (!list().contains(item)) throw CompressSharpException("only provide path of currently viewed items")

    if (path.isEmpty) item.getName // avoid leading separator
    else path.mkString(start = "", sep = sep, end = sep).concat(item.getName)
  }

  private def buildTrie(entries: Seq[Entry]): TrieTree[String, Item] = {
    val trie = TrieTree.empty[String, Item]((null, null))
    entries foreach {
      entry =>
        val path = entry.path.replace("\\", "/").split("/")
        val values = Vector.tabulate(path.size - 1)(i => new Item(path(i))) :+ new Item(entry)
        trie.insert(path.zip(values).toList)
    }
    trie
  }

  def forward(child: Item): Boolean = {
    if (!child.isDir) return false // can't enter file item

    cur.children.find(_.value == child) match {
      case Some(c) =>
        cur = c
        path.push(c.key)
        updateBuffer()
        true
      case None =>
        throw CompressSharpException(s"$cur doesn't have a child ${child.getName}")
    }
  }

  def back(): Boolean = {
    if (cur.isRoot) false
    else {
      path.pop()
      cur = cur.parent
      updateBuffer()
      true
    }
  }

  private def updateBuffer(): Unit = {
    clear()
    addAll(list())
  }

  def list(): Set[Item] = {
    cur.children.map(_.value)
  }
}
