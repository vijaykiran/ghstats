import java.nio.file.{DirectoryStream, Files, Path, Paths}
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.collection.JavaConverters._
import scala.io.Source

object GithubSimple {
  implicit val formats = DefaultFormats
  val path = Paths.get("github")
  def main(args: Array[String]): Unit = {
    val ghDirStream: DirectoryStream[Path] = Files.newDirectoryStream(path)
    val filesList: Iterable[Path] = ghDirStream.asScala
    val eventCounts: Iterable[Map[String, BigDecimal]] = filesList.map(eventCountForFile)
    val finalCounts: Map[String, BigDecimal] = eventCounts.foldLeft(Map[String, BigDecimal]())(addMaps)

    finalCounts.foreach { case (k, v) => println(s"$v - $k")}
    //Make sure that dirStream is closed
    ghDirStream.close()
  }

  def eventCountForFile(p: Path): Map[String, BigDecimal] = {
    println(s"Working on file  $p")
    Source.fromFile(p.toString).getLines().foldLeft(Map[String, BigDecimal]()) {
      (state: Map[String, BigDecimal], current: String) =>
        val eventName = (parse(current) \ "type").extract[String]
        addMaps(state, Map(eventName -> 1))
    }
  }

  def addMaps(m1: Map[String, BigDecimal], m2: Map[String, BigDecimal]): Map[String, BigDecimal] =
    m1 ++ m2.map { case (k, v) => k -> (v + m1.getOrElse(k, 0)) }

}
