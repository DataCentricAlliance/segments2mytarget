package net.facetz.mailru.helper

import net.facetz.mailru.ConfigHolder
import net.facetz.mailru.api.MailRuApiProvider

trait MailRuApiConfigProvider {
  this: MailRuApiProvider =>

  private val config = ConfigHolder.getConfiguration

  override protected val clientId = config.clientId
  override protected val clientSecret = config.clientSecret
}