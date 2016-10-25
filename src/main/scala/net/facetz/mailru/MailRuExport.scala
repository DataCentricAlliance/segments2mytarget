package net.facetz.mailru

import java.io.File

import net.facetz.mailru.auditory.{MailRuAuditoryCleaner, MailRuAuditoryUpdater}
import net.facetz.mailru.helper.{MailRuApiConfigProvider, MailRuSegmentFileConfigProvider, SimpleLogger}
import net.facetz.mailru.segment.{MailRuSegmentFileProcessor, MailRuSegmentFileUploader, SegmentFileProvider}

import scala.util.control.NonFatal

object MailRuExport extends SimpleLogger {

  class Exporter extends SegmentFileProvider with MailRuSegmentFileUploader with MailRuApiConfigProvider {
    // SegmentFileProvider
    override protected val allowedSegments = ConfigHolder.config.allowedSegments
    override protected val processedFolder = ConfigHolder.config.processedFolder
    override protected val workingDirectory = ConfigHolder.config.workingDirectory

    // MailRuSegmentFileUploader
    override protected val dateStr = ConfigHolder.config.dateStr
    override protected val extractToBaseList = ConfigHolder.config.extractToBaseList
  }

  class FilesTransformer extends MailRuSegmentFileProcessor with MailRuSegmentFileConfigProvider {
    override protected def process(fileBySegmentId: Map[String, Seq[File]]): Unit = {}
  }

  class TransformerAndExporter
    extends MailRuSegmentFileProcessor
    with MailRuSegmentFileConfigProvider
    with MailRuSegmentFileUploader
    with MailRuApiConfigProvider {
    override protected val extractToBaseList = ConfigHolder.config.extractToBaseList
  }

  val auditoryUpdater = new MailRuAuditoryUpdater with MailRuApiConfigProvider {
    override protected val dateStr = ConfigHolder.config.dateStr
  }

  val cleaner = new MailRuAuditoryCleaner with MailRuApiConfigProvider {
    override protected val periodInDays = ConfigHolder.config.expiryPeriodInDays
  }

  def run(): Unit = {
    log.info(s"mailRuExporter running")

    try {
      if (ConfigHolder.config.auditoryUpdate) {
        log.info("Starting AuditoryUpdater")
        auditoryUpdater.run()
      } else {
        if (ConfigHolder.config.process && ConfigHolder.config.upload) {
          log.info("Starting TransformerAndExporter")
          new TransformerAndExporter().startProcessing()
        } else if (ConfigHolder.config.process) {
          log.info("Starting FilesTransformer")
          new FilesTransformer().startProcessing()
        } else if (ConfigHolder.config.upload) {
          log.info("Starting Exporter")
          new Exporter().startProcessing()
        } else if (ConfigHolder.config.clean) {
          log.info("Starting Cleaner")
          cleaner.run()
        } else {
          log.error(s"Bad flags mode: ${ConfigHolder.config}")
        }
      }
    } catch {
      case NonFatal(t) => log.error("Some error detected", t)
    }

    log.info("mailRuExporter finished!")
  }

}
