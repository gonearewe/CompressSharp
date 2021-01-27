package fun.mactavish.compress_sharp

import scala.collection.mutable

case class TrieTree[K, V](
                           parent: TrieTree[K, V],
                           key: K,
                           value: V,
                           private val children_ : mutable.Set[TrieTree[K, V]] = mutable.Set[TrieTree[K, V]]()
                         ) {
  def children: Set[TrieTree[K, V]] = children_.toSet

  def insert(tail: List[(K, V)]): TrieTree[K, V] = {
    tail match {
      case Nil => this
      case next :: remaining =>
        children_.find(_.key == next._1) match {
          case Some(child) =>
            child.insert(remaining)
          case None =>
            val child = TrieTree(this, next._1, next._2)
            children_.add(child)
            child.insert(remaining)
        }
        this
    }
  }

  def search(path: List[K]): Option[TrieTree[K, V]] = {
    path match {
      case Nil => Some(this)
      case next :: remaining =>
        this.children_.find(_.key == next) match {
          case Some(child) => child.search(remaining)
          case None => None
        }
    }
  }

  def isRoot = this.parent == null

}

object TrieTree {
  def empty[K, V](identity: (K, V)): TrieTree[K, V] =
    TrieTree[K, V](parent = null, key = identity._1, value = identity._2)
}
