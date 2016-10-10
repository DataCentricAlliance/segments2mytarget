package net.facetz.mailru.helper

import net.facetz.mailru.ConfigHolder
import net.facetz.mailru.segment.MailRuSegmentFileProcessor

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