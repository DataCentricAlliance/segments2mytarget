package net.facetz.mailru

import java.io.File

import net.facetz.mailru.helper.{MailRuApiConfigProvider, SimpleLogger}
import net.facetz.mailru.segment.{MailRuSegmentFileProcessor, MailRuSegmentFileUploader}

object MailRuExport extends SimpleLogger {

  trait MailRuSegmentFileConfigProvider {
    this: MailRuSegmentFileProcessor =>

    private val config = ConfigHolder.getConfiguration

    override protected val dateStr = config.dateStr
    override protected val outputFolderName = config.outputFolderName

    override protected def workingDirectory: String = config.workingDirectory

    override protected def filenamePatter: String = config.regexp

    override protected def mailRuPartnerPrefix: String = config.partnerPrefix
  }

  class Exporter extends MailRuSegmentFileProcessor with MailRuSegmentFileConfigProvider
    with MailRuSegmentFileUploader with MailRuApiConfigProvider

  class FilesTransformer extends MailRuSegmentFileProcessor with MailRuSegmentFileConfigProvider {
    override protected def process(fileBySegmentId: Map[String, Seq[File]]): Unit = {}
  }


  def run: Unit = {
    log.info("mailRuExporter running...")

    if(ConfigHolder.getConfiguration.upload) {
      new Exporter().startProcessing()
    } else {
      new FilesTransformer().startProcessing()
    }

    log.info("mailRuExporter finished!")
  }

}
