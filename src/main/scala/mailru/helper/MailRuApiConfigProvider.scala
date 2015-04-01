package net.facetz.export.mr.mailru.helper

import mailru.ConfigHolder
import net.facetz.export.mr.mailru.api.MailRuApiProvider

trait MailRuApiConfigProvider {
  this: MailRuApiProvider =>

  private val config = ConfigHolder.getConfiguration

  override protected val clientId = config.clientId
  override protected val clientSecret = config.clientSecret
}