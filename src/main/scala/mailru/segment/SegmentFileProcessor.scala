package net.facetz.export.mr.mailru.segment

import java.io.File

trait SegmentFileProcessor {

  protected def dateStr: String

  protected def process(fileBySegmentId: Map[String, Seq[File]]): Unit

}
