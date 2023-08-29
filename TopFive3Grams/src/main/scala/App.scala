import upickle.default._

import scala.collection.mutable.ListBuffer
import scala.io.Source

case class Actor(display_login: String)
object Actor{
  implicit val rw: ReadWriter[Actor] = macroRW
}

case class Commit(message: String)
object Commit{
  implicit val rw: ReadWriter[Commit] = macroRW
}

case class Payload(commits: List[Commit])
object Payload{
  implicit val rw: ReadWriter[Payload] = macroRW
}

case class Item(actor: Actor, payload:Payload)
object Item{
  implicit val rw: ReadWriter[Item] = macroRW
}

object Program {
  def main(args: Array[String]) = {
    val lines : Iterator[String] = ExtractFileContent("10K.github.jsonl")

    val commitsPerAuthor = CommitsPerAuthor(lines)

    val topFiveTrigramsPerAuthor = commitsPerAuthor
      .map(x => x._1 -> TopFiveTrigrams(x._2))

    topFiveTrigramsPerAuthor.foreach { case (author: String, trigrams: Seq[String]) =>
      val topFiveTrigrams = trigrams.map("'" + _ + "'").mkString(" ")
      println(s"'$author' $topFiveTrigrams")
    }
  }

  private def ExtractFileContent(fileName: String) = {
    val file = Source.fromFile("10K.github.jsonl")
    file.getLines
  }

  private def CommitsPerAuthor(lines : Iterator[String]) = lines
    .map(line => try { read[Item](line) } catch { case ex => null })
    .filter(item => item != null && item.payload.commits.nonEmpty)
    .toSeq
    .groupBy(_.actor.display_login)
    .map { case (author: String, items: Seq[Item]) =>
      author -> items.flatMap(_.payload.commits).map(_.message)
    }

  private def TopFiveTrigrams(messages: Seq[String]) = {
    val trigrams = messages.flatMap(MessageTrigrams(_))

    trigrams
      .groupMapReduce(identity)(_ => 1)(_ + _)
      .toSeq
      .sortWith(_._2 > _._2)
      .map(_._1)
      .take(5)
  }

  private def MessageTrigrams(message: String) = {
    val words = message.split("\\W+")

    val buffer = new ListBuffer[String]()
    for (i <- 0 until words.length - 2) {
      buffer += s"${words(i)} ${words(i + 1)} ${words(i + 2)}"
    }

    buffer.toSeq
  }
}
