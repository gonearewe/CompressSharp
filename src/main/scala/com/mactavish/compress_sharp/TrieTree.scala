package com.mactavish.compress_sharp

import scala.collection.immutable.{AbstractSeq, LinearSeq}
import scala.collection.mutable

case class TrieTree[T](
                        parent: TrieTree[T],
                        value: T,
                        private val children: mutable.Set[TrieTree[T]] = mutable.Set[TrieTree[T]]()
                      ) {
  def insert(tail: Seq[T]): TrieTree[T] = {
    tail match {
      case Nil => this
      case next :: remaining =>
        children.find(_.value == next) match {
          case Some(child) =>
            child.insert(remaining)
          case None =>
            val child = TrieTree(this, next)
            children.add(child)
            child.insert(remaining)
        }
        this
    }
  }

}

object TrieTree {
  def empty[T](identity:T): TrieTree[T] = TrieTree[T](parent = null, value = identity)
}
