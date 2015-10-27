import java.nio.file.{DirectoryStream, Files, Path, Paths}

import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source

object GithubFutures {
  implicit val formats = DefaultFormats
  val path = Paths.get("github")

  def main(args: Array[String]): Unit = {
    val ghDirStream: DirectoryStream[Path] = Files.newDirectoryStream(path)
    val filesList: Seq[Path] = ghDirStream.asScala.toSeq
    val eventCounts: Future[Seq[Map[String, BigDecimal]]] = Future.sequence(filesList.map(eventCountForFile))

    val finalCountsFuture: Future[Map[String, BigDecimal]] = eventCounts.map {
      _.foldLeft(Map[String, BigDecimal]())(addMaps)
    }

    val result: Map[String, BigDecimal] = Await.result(finalCountsFuture, 1.minute)
    result.foreach { case (k, v) => println(s"$v - $k") }

    //Make sure that dirStream is closed
    ghDirStream.close()
  }

  def eventCountForFile(p: Path): Future[Map[String, BigDecimal]] = Future {
    println(s"Working on file  $p")
    val source = Source.fromFile(p.toString)
    source.getLines().foldLeft(Map[String, BigDecimal]()) {
      (state: Map[String, BigDecimal], current: String) =>
        val eventName: String = (parse(current) \ "type").extract[String]
        addMaps(state, Map(eventName -> 1))
    }
  }

  def addMaps(m1: Map[String, BigDecimal], m2: Map[String, BigDecimal]): Map[String, BigDecimal] =
    m1 ++ m2.map { case (k, v) => k -> (v + m1.getOrElse(k, 0)) }

}
