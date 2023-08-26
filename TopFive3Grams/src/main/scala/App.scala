import upickle.default._

import scala.collection.mutable.ListBuffer
import scala.io.Source

case class Actor(display_login: String)
case class Commit(message: String)
case class Payload(commits: List[Commit])
case class Item(actor: Actor, payload:Payload)

object Program {
  def main(args: Array[String]) = {
    val file = Source.fromFile("10K.github.jsonl")
    val lines : Iterator[String] = file.getLines

    implicit val actorRw: ReadWriter[Actor] = macroRW
    implicit val commitRw: ReadWriter[Commit] = macroRW
    implicit val payloadRw: ReadWriter[Payload] = macroRW
    implicit val itemRw: ReadWriter[Item] = macroRW

    val commitsPerAuthor = lines
      .map( line => try { read[Item](line) } catch { case e => null })
      .filter(item => item != null && item.payload.commits.nonEmpty)
      .toSeq
      .groupBy(_.actor.display_login)
      .map(item => item._1 -> item._2.flatMap(_.payload.commits).map(_.message))

    val topFiveTrigramsPerAuthor = commitsPerAuthor
      .map(x => x._1 -> GetTopFiveTrigrams(x._2))

    topFiveTrigramsPerAuthor.foreach { x =>
      val trigrams = x._2.map("'" + _ + "'").mkString(" ")
      println(s"'${x._1}' $trigrams")
    }
  }

  private def GetTopFiveTrigrams(messages: Seq[String]) = {
    val trigrams = messages.flatMap(GetMessageTrigrams(_))

    trigrams
      .groupMapReduce(identity)(_ => 1)(_ + _)
      .toSeq
      .sortWith(_._2 > _._2)
      .map(_._1)
      .take(5)
  }

  private def GetMessageTrigrams(message: String) = {
    val words = message.split("\\W+")

    val buffer = new ListBuffer[String]()
    for (i <- 0 until words.length - 2) {
      buffer += s"${words(i)} ${words(i + 1)} ${words(i + 2)}"
    }

    buffer.toSeq
  }
}
