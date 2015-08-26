package net.facetz.mailru

import java.io.File

import net.facetz.mailru.auditory.{MailRuAuditoryCleaner, MailRuAuditoryUpdater}
import net.facetz.mailru.helper.{MailRuApiConfigProvider, SimpleLogger}
import net.facetz.mailru.segment.{MailRuSegmentFileProcessor, MailRuSegmentFileUploader, SegmentFileProvider}

object MailRuExport extends SimpleLogger {

  trait MailRuSegmentFileConfigProvider {
    this: MailRuSegmentFileProcessor =>

    private val config = ConfigHolder.config

    override protected val dateStr = config.dateStr
    override protected val processedFolder = config.processedFolder

    override protected def workingDirectory: String = config.workingDirectory

    override protected def filenamePatter: String = config.regexp

    override protected def mailRuPartnerPrefix: String = config.partnerId

    override protected def maxThreshold: Int = config.maxThreshold
  }

  class Exporter extends SegmentFileProvider with MailRuSegmentFileUploader with MailRuApiConfigProvider {
    override protected val allowedSegments = ConfigHolder.config.allowedSegments
    override protected val processedFolder = ConfigHolder.config.processedFolder
    override protected val workingDirectory = ConfigHolder.config.workingDirectory
    override protected val dateStr = ConfigHolder.config.dateStr
  }


  class TransformerAndExporter
    extends MailRuSegmentFileProcessor
    with MailRuSegmentFileConfigProvider
    with MailRuSegmentFileUploader
    with MailRuApiConfigProvider

  class FilesTransformer extends MailRuSegmentFileProcessor with MailRuSegmentFileConfigProvider {
    override protected def process(fileBySegmentId: Map[String, Seq[File]]): Unit = {}
  }

  val auditoryUpdater = new MailRuAuditoryUpdater with MailRuApiConfigProvider {
    override protected val dateStr = ConfigHolder.config.dateStr
  }

  def run(): Unit = {
    log.info("mailRuExporter running...")

    var possibleBadFlags = false

    if (ConfigHolder.config.auditoryUpdate) {
      auditoryUpdater.run()
    } else {
      if (ConfigHolder.config.process && ConfigHolder.config.upload) {
        new TransformerAndExporter().startProcessing()
      } else if (ConfigHolder.config.process) {
        new FilesTransformer().startProcessing()
      } else if (ConfigHolder.config.upload) {
        new Exporter().startProcessing()
      } else {
        possibleBadFlags = true
      }
    }

    if (ConfigHolder.config.clean && ConfigHolder.config.expiryPeriodInDays.isDefined) {
      val cleaner = new MailRuAuditoryCleaner with MailRuApiConfigProvider {
        override protected val periodInDays = ConfigHolder.config.expiryPeriodInDays.get
      }
      cleaner.run()
    } else {
      if (ConfigHolder.config.clean && ConfigHolder.config.expiryPeriodInDays.isEmpty) {
        log.error("expiry period (-e) should be specified in order to perform cleaning")
      }
      if (possibleBadFlags) {
        log.error(s"bad flags mode: ${ConfigHolder.config}")
      }
    }

    log.info("mailRuExporter finished!")
  }

}
