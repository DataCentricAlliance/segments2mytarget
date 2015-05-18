package net.facetz.mailru.segment

import java.io._
import java.util.zip.GZIPInputStream

import net.facetz.mailru.helper.SimpleLogger

import scala.collection.mutable
import scala.io.Source
import scala.reflect.io.Path
import scala.util.control.NonFatal

trait MailRuSegmentFileProcessor extends SegmentFileProcessor with SimpleLogger {

  protected def workingDirectory: String

  protected def processedFolder: String

  protected def filenamePatter: String

  protected def mailRuPartnerPrefix: String


  protected val maxThreshold = 5000000
  protected val minThreshold = 5000

  // [segmentId, (isFull, _, _)]
  protected val writerBySegment: mutable.MultiMap[String, (BufferedWriter, File)] = new mutable.HashMap[String,
    mutable.Set[(BufferedWriter, File)]] with mutable.MultiMap[String, (BufferedWriter, File)]

  protected val fileLinesCount = mutable.Map.empty[File, Int]
  protected val indexBySegmentId = mutable.Map.empty[String, Int]

  def startProcessing(): Unit = {
    try {
      val filesToProcess = Path(workingDirectory).toDirectory.list.filter(_.name.matches(filenamePatter))
      processSourceFiles(filesToProcess)

      def extractFile(tuple: (_, File)): File = tuple._2
      def isFileTooSmall(tuple: (_, File)): Boolean = fileLinesCount(tuple._2) < minThreshold
      def segmentHasFiles(tuple: (_, Seq[File])): Boolean = tuple._2.nonEmpty

      val resultMap = writerBySegment
        .mapValues(_.filterNot(isFileTooSmall).map(extractFile).toSeq)
        .filter(segmentHasFiles)
        .toMap

      process(resultMap)
    } catch {
      case NonFatal(t) => log.error(s"files processing error: ${t.getMessage}", t)
    } finally {
      destroy()
    }
  }

  protected def destroy(): Unit = {
    writerBySegment.values.flatten.foreach(t => t._1.close())
  }

  protected def processSourceFiles(filesToProcess: Iterator[Path]): Unit = {
    if (filesToProcess.isEmpty) {
      log.error("source files not found")
    } else {
      filesToProcess.foreach(p => {
        log.info(s"process : $p")
        getSourceByPath(p).getLines().foreach(line => {
          // oNGb1XM2TjCeZyK2GH8Y8A\t1975:1.0;1943:1.0
          val Array(uid, segmentIdsStr) = line.split('\t')
          segmentIdsStr.split(';').foreach(segmentInfo => {
            segmentInfo.split(":") match {
              case Array(segmentIdStr, _) =>
                val segmentId = segmentIdStr
                appendMailRuSegmentUser(segmentId, uid)
              case _ => log.error(s"segment id not resolved: $line")
            }
          })
        })
      })
    }
  }

  protected def getSourceByPath(p: Path): Source = {
    Source.fromInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(p.path))))
  }

  protected def appendMailRuSegmentUser(segmentId: String, uid: String): Unit = {
    val (writer, file) = getValidWriter(segmentId).getOrElse {
      addWriter(segmentId)
    }
    writer.append(uid)
    writer.newLine()

    fileLinesCount(file) = fileLinesCount.getOrElse(file, 0) + 1
  }

  protected def hasValidWriter(segmentId: String): Boolean = {
    getValidWriter(segmentId) match {
      case Some(x) => true
      case None => false
    }
  }

  protected def getValidWriter(segmentId: String): Option[(BufferedWriter, File)] = {
    if (writerBySegment.contains(segmentId)) {
      writerBySegment(segmentId)
        .find {
        case (_, file) => fileLinesCount.contains(file) && fileLinesCount(file) <= maxThreshold
      }
    } else {
      None
    }
  }

  protected def addWriter(segmentId: String): (BufferedWriter, File) = {
    indexBySegmentId(segmentId) = indexBySegmentId.getOrElse(segmentId, -1) + 1
    val index: Int = indexBySegmentId(segmentId)
    val file = new File(s"$workingDirectory/$processedFolder/facetz-segment_${segmentId}_${dateStr}_$index.txt")
    val parent = file.getParentFile
    parent.mkdirs()
    file.createNewFile()

    val writer = new BufferedWriter(new FileWriter(file))
    writerBySegment.addBinding(segmentId, (writer, file))

    writer.append(mailRuPartnerPrefix)
    writer.newLine()
    (writer, file)
  }

}
