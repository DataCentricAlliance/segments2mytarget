package net.facetz.mailru.segment

import java.io.File

import net.facetz.mailru.api._
import net.facetz.mailru.helper.SimpleLogger

import scala.annotation.tailrec
import scala.util.{Failure, Success}

trait MailRuSegmentFileUploader extends SegmentFileProcessor with MailRuApiProvider with SimpleLogger {

  protected def extractToBaseList: Boolean

  protected val uploadAwaitTimeoutMillis = 2 * 60 * 1000

  override protected def process(filesBySegmentId: Map[String, Seq[File]]): Unit = {
    if (filesBySegmentId.isEmpty) {
      log.info("No files to upload")
    } else {
      log.info(s"File upload started for ${filesBySegmentId.size} segments: ${filesBySegmentId.keys.toList}")
      getAuthToken match {
        case Some(token) =>
          log.info("Auth token - ok")
          log.info(s"Try upload to users to existing segment UserLists: $extractToBaseList")
          uploadSegmentFiles(token, filesBySegmentId)
          log.info("File upload finished!")
        case None => throw new IllegalArgumentException("Can't get authToken")
      }
    }
  }

  protected def uploadSegmentFiles(token: String, filesBySegmentId: Map[String, Seq[File]]): Unit = {
    val segmentBaseLists = if (extractToBaseList) {
      getRemarketingUsersList(token).map { userLists =>
        val segmentsMap = userLists.flatMap { list =>
          val segmentId = userListNameToSegmentId(list.name)
          segmentId.map((_, list.id))
        }.toMap
        log.info(s"Fetched Map(segmentId -> UserList name) of existing UserLists $segmentsMap")
        segmentsMap
      }.getOrElse {
        log.info("Could not extract matchings of existing UserLists to segments")
        Map.empty[String, Int]
      }
    } else {
      Map.empty[String, Int]
    }

    for {(segmentId, files) <- filesBySegmentId} {
      files.zipWithIndex.foreach {
        case (file, index) =>
          val baseListId = segmentBaseLists.getOrElse(segmentId, 0)
          uploadSegmentFileWithRetry(token, file, file.getName, baseListId)
      }
    }
  }

  @tailrec
  private def uploadSegmentFileWithRetry(token: String, file: File, name: String, baseListId: Int): Unit = {
    log.info(s"Try to upload ${file.getPath} ${if (baseListId != 0) s"into UserList $baseListId" else ""}")
    uploadSegmentFile(token, file, name, baseListId) match {
      case Success(x) if x.isRight =>
        val logMessage = if (baseListId == 0) {
          s"Uploaded ${file.getPath} into UserList id ${x.right.get.id}"
        } else if (baseListId > 0) {
          s"Uploaded ${file.getPath} into UserList id $baseListId. Diff list id ${x.right.get.id}"
        } else {
          s"Removed ${file.getPath} from UserList id $baseListId. Diff list id ${x.right.get.id}"
        }
        log.info(logMessage)
      case Success(x) if x.isLeft =>
        log.info(s"Limit for ${file.getPath}. Waiting ${uploadAwaitTimeoutMillis / 1000} seconds")
        Thread.sleep(uploadAwaitTimeoutMillis)
        uploadSegmentFileWithRetry(token, file, name, baseListId)
      case Failure(f) =>
        log.error(s"Can't upload file ${file.getPath}", f)
    }
  }

  private def userListNameToSegmentId(name: String): Option[String] = {
    name.split("_") match {
      case Array("facetz-segment", segmentId, _*) =>
        Some(segmentId)
      case _ =>
        log.warn(s"Could not extract segment id from UserList name $name")
        None
    }
  }

}
