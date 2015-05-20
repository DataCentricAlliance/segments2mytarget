package net.facetz.mailru.segment

import java.io.File

import scala.collection.mutable
import scala.reflect.io.Path

trait SegmentFileProvider extends SegmentFileProcessor {

  protected def allowedSegments: Option[List[String]]
  protected def processedFolder: String
  protected def workingDirectory: String

  def startProcessing(): Unit = {
    val processedSegmentFiles = Path(s"$workingDirectory/$processedFolder").toDirectory.list
    val resultFiles = pathsToFilesMap(processedSegmentFiles)
    process(resultFiles)
  }

  protected def pathsToFilesMap(paths: Iterator[Path]): Map[String, Seq[File]] = {
    val filesContainer: mutable.MultiMap[String, File] = new mutable.HashMap[String, mutable.Set[File]] with mutable.MultiMap[String, File]
    paths
      .map(t => t.name.split('_') -> t.jfile)
      .map({ case (Array(_, segmentId, dateStr, _), file) => (segmentId, dateStr) -> file })
      .filter({
        case ((segmentId, _), _) => allowedSegments match {
          case Some(list) => list.contains(segmentId)
          case None => true
        }})
      .filter({ case ((_, dateString), _) => dateString == dateStr })
      .foreach({ case ((segmentId, _), file) => filesContainer.addBinding(segmentId, file) })

    filesContainer.toMap.mapValues(_.toSeq)
  }

}
