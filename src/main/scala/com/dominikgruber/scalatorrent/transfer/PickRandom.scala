package com.dominikgruber.scalatorrent.transfer

import scala.annotation.tailrec
import scala.util.Random

object PickRandom {

  implicit class RichSeq[T](seq: Seq[T]) {

    def randomElement: Option[T] = randomIndex.map(seq)

    def randomIndex: Option[Int] = seq.size match {
      case 0 => None
      case size  => Some(Random.nextInt(size))
    }

    def randomIndexOf(value: T): Option[Int] = {

      def hasAnyIn(range: Range): Boolean =
        seq.slice(range.start, range.end + 1)
          .contains(value)

      def splitInHalf(range: Range): (Range, Range) = {
        val mid = range.size / 2
        val firstHalf = range.take(mid)
        val secondHalf = range.drop(mid)
        (firstHalf, secondHalf)
      }

      @tailrec
      def pickIn(range: Range): Option[Int] = range match {
        case Seq() =>
          None
        case Seq(index) =>
          if (seq.lift(index).contains(value)) Some(index) else None
        case _ =>
          val (firstHalf, secondHalf) = splitInHalf(range)
          (hasAnyIn(firstHalf), hasAnyIn(secondHalf)) match {
            case (false, false) => None
            case (true, false) => pickIn(firstHalf)
            case (false, true) => pickIn(secondHalf)
            case (true, true) =>
              if (Random.nextBoolean) pickIn(firstHalf)
              else pickIn(secondHalf)
          }
      }

      pickIn(seq.indices)
    }
  }

}
