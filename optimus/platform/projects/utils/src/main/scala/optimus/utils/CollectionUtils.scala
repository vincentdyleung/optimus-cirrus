/*
 * Morgan Stanley makes this available to you under the Apache License, Version 2.0 (the "License").
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package optimus.utils

import java.{util => ju}

import optimus.utils.CollectionUtils.TraversableOps

import scala.collection.SeqLike
import scala.collection.immutable.Seq
import scala.util._

object CollectionUtils extends CollectionUtils {
  final class TraversableOps[A](val as: Traversable[A]) extends AnyVal {

    /**
     * return 0 if empty, 1 if size 1, or 2 if size > 1
     */
    private[this] def zeroOneOrMany: Int = as match {
      case x: SeqLike[_, _] @unchecked =>
        val lc = x.lengthCompare(1)
        if (lc < 0) 0
        else if (lc == 0) 1
        else 2
      case x =>
        val it = x.toIterator
        if (!it.hasNext) 0
        else {
          it.next()
          if (it.hasNext) 2 else 1
        }
    }

    /**
     * This returns an object of type A
     * if this is the only element of the Traversable.
     * Otherwise an exception is thorwn.
     *
     */
    def single: A = zeroOneOrMany match {
      case 0 => throw new NoSuchElementException("single on empty Traversable")
      case 1 => as.head
      case _ => throw new IllegalArgumentException(s"single on multi-element Traversable: $as")
    }

    /** Returns Some(x) if there is a single element or None if empty. If more than 1 elements are found, this method throws. */
    def singleOption: Option[A] = zeroOneOrMany match {
      case 0 | 1 => as.headOption
      case _     => throw new IllegalArgumentException(s"singleOption on multi-element Traversable: $as")
    }

    /** Returns Some(x) if there is a single element. If there are 0 or more than 1 elements, this method returns None. */
    def singleOrNone: Option[A] = zeroOneOrMany match {
      case 1 => Some(as.head)
      case _ => None
    }

    /**
     * An element of type A is returned if the Traversable is a singleton.
     * Otherwise an exception flies.
     *
     * @param f a function that composes an exception message according to type A.
     * @return
     */
    def singleOrThrow(f: Traversable[A] => String): A =
      if (zeroOneOrMany == 1) as.head
      else throw new IllegalArgumentException(f(as))

    /** Returns Some(x) if there is a single element or None if empty. If more than 1 elements are found, use fallback */
    def singleOptionOr(fallback: Option[A]): Option[A] =
      if (zeroOneOrMany < 2) as.headOption else fallback

    /**
     * Returns true if the Traversable contains one element ( possibly multiple times ).
     * Returns false otherwise.
     */
    def isSingleDistinct: Boolean = zeroOneOrMany match {
      case 0 => false
      case 1 => true
      case _ => as.toSet.size == 1
    }

    /**
     * Returns an element of type A if this is the only member of the Traversable.
     * Otherwise an exception is thrown.
     */
    def singleDistinct: A = zeroOneOrMany match {
      case 0 => throw new IllegalArgumentException("Expected single element, but was empty!")
      case 1 => as.head
      case _ =>
        as.toSet.size match {
          case 1 => as.head
          case n =>
            throw new IllegalArgumentException(
              s"Expected single distinct element, but found $n: [${as.mkString(",")}]")
        }
    }

    /**
     * Returns None if the Traversable is empty.
     * Returns Some(a) if a is the only element of type A contained by the Traversable.
     * Otherwise exception flies.
     * @return
     */
    def singleDistinctOption: Option[A] =
      if (zeroOneOrMany < 2) as.headOption
      else
        as.toSet.size match {
          case 0 | 1 => as.headOption
          case n =>
            throw new IllegalArgumentException(
              s"Expected zero or one distinct element, but found $n: [${as.mkString(",")}]")
        }


    /** Not async friendly, for that see singleOrElseAsync. */
    def singleOptionOrElse(nonSingleHandler: (Int, Traversable[A]) => Option[A]): Option[A] = as.size match {
      case 1 => as.headOption
      case n => nonSingleHandler(n, as)
    }


    /** Not async friendly, for that see singleOrElseAsync. */
    def singleOr(nonSingleHandler: => A): A = as.size match {
      case 1 => as.head
      case n => nonSingleHandler
    }

    /** Not async friendly, for that see singleOrElseAsync. */
    def singleOrElse(nonSingleHandler: (Int, Traversable[A]) => A): A = as.size match {
      case 1 => as.head
      case n => nonSingleHandler(n, as)
    }
  }
}

// note that this is inherited by optimus.platform package object, so you only need to import it if you can't see platform
trait CollectionUtils {
  implicit def traversable2Ops[A](as: Traversable[A]): TraversableOps[A] = new TraversableOps(as)

  implicit class TraversableTuple2Ops[A, B](iterable: Traversable[(A, B)]) {
    def toSingleMap: Map[A, B] = iterable.groupBy(_._1).map {
      case (k, kvs) => k -> kvs.map { case (_, v) => v }.single
    }

    def toDistinctMap: Map[A, B] = iterable.groupBy(_._1).map {
      case (k, kvs) => k -> kvs.map { case (_, v) => v }.toSeq.distinct.single
    }

    def toGroupedMap: Map[A, Seq[B]] = iterable.groupBy(_._1).map {
      case (k, kvs) => k -> kvs.map { case (_, v) => v }.to[Seq]
    }

    def toGroupedMap[C](f: Traversable[B] => C): Map[A, C] =
      iterable.groupBy { case (a, b) => a }.transform((_, abs) => f(abs.map { case (a, b) => b }))
  }

  implicit class juPropertiesOps(props: ju.Properties) {
    def addAll[K <: AnyRef, V <: AnyRef](all: ju.Map[K, V]): Unit = { // hack against scala/bug#10418 (fixed in 2.13 only)
      (props: ju.Hashtable[AnyRef, AnyRef]).putAll(all)
    }
  }
  implicit class OptimusOptionOps[A](underlying: Option[A]) {
    def getOrThrow(msg: => String): A = underlying.getOrElse(throw new IllegalArgumentException(msg))
    def onEmpty(action: => Unit): Option[A] = {
      if (underlying.isEmpty) action else (); underlying
    }
  }

  implicit class OptimusTryOps[A](underlying: Try[A]) {
    def getOrThrow(msg: => String): A = underlying match {
      case Success(value)     => value
      case Failure(exception) => throw new IllegalArgumentException(s"$msg. See details: $exception.")
    }
    def onEmpty(action: Throwable => Unit): Try[A] = onFailure(action)
    def onFailure(action: Throwable => Unit): Try[A] = {
      if (underlying.isFailure) action(underlying.failed.get) else ();
      underlying
    }
  }
}