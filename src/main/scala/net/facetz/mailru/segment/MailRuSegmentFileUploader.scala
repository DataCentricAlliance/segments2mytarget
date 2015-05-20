package net.facetz.mailru.segment

import java.io.File

import net.facetz.mailru.api._
import net.facetz.mailru.helper.SimpleLogger

import scala.annotation.tailrec
import scala.util.{Failure, Success}

trait MailRuSegmentFileUploader extends SegmentFileProcessor with MailRuApiProvider with SimpleLogger {

  override protected def process(filesBySegmentId: Map[String, Seq[File]]): Unit = {
    if (filesBySegmentId.isEmpty) {
      log.info("no files to upload")
    } else {
      log.info(s"file upload started for ${filesBySegmentId.size} segments...")
      getAuthToken match {
        case Some(token) =>
          log.info("auth token - ok")
          uploadSegmentFiles(token, filesBySegmentId)
        case None => throw new IllegalArgumentException("can't get authToken")
      }
      log.info("file upload finished!")
    }
  }

  protected def uploadSegmentFiles(token: String, filesBySegmentId: Map[String, Seq[File]]): Unit = {
    for {(segmentId, files) <- filesBySegmentId} {
      files.zipWithIndex.foreach {
        case (file, index) => uploadSegmentFileWithRetry(token, file, file.getName)
      }
    }
  }

  @tailrec
  private def uploadSegmentFileWithRetry(token: String, file: File, name: String): Unit = {
    log.info(s"Try to upload ${file.getPath}")
    uploadSegmentFile(token, file, name) match {
      case Success(x) if x.isRight =>
        log.info(s"Uploaded ${file.getPath}")
      case Success(x) if x.isLeft =>
        log.info(s"limit for ${file.getPath}. Waiting...")
        Thread.sleep(2 * 60 * 1000)
        uploadSegmentFileWithRetry(token, file, name)
      case Failure(f) =>
        log.error(s"can't upload file ${file.getPath}", f)
    }
  }

}
