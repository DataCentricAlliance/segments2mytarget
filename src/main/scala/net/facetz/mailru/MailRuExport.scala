package net.facetz.mailru

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import net.facetz.mailru.auditory.MailRuAuditoryUpdater
import net.facetz.mailru.helper.{MailRuApiConfigProvider, SimpleLogger}
import net.facetz.mailru.segment.{MailRuSegmentFileProcessor, MailRuSegmentFileUploader}

object MailRuExport extends SimpleLogger {

  trait MailRuSegmentFileConfigProvider {
    this: MailRuSegmentFileProcessor =>

    private val config = ConfigHolder.config

    override protected val dateStr = config.dateStr
    override protected val outputFolderName = config.outputFolderName

    override protected def workingDirectory: String = config.workingDirectory

    override protected def filenamePatter: String = config.regexp

    override protected def mailRuPartnerPrefix: String = config.partnerId
  }

  class Exporter extends MailRuSegmentFileProcessor with MailRuSegmentFileConfigProvider
    with MailRuSegmentFileUploader with MailRuApiConfigProvider

  class FilesTransformer extends MailRuSegmentFileProcessor with MailRuSegmentFileConfigProvider {
    override protected def process(fileBySegmentId: Map[String, Seq[File]]): Unit = {}
  }

  val auditoryUpdater = new MailRuAuditoryUpdater with MailRuApiConfigProvider {
    override protected val dateStr = ConfigHolder.config.dateStr
  }

  def run: Unit = {
    log.info("mailRuExporter running...")

    if(ConfigHolder.config.auditoryUpdate) {
      auditoryUpdater.run()
    } else {
      if(ConfigHolder.config.upload) {
        new Exporter().startProcessing()
      } else {
        new FilesTransformer().startProcessing()
      }
    }

    log.info("mailRuExporter finished!")
  }

}
