package net.facetz.mailru.helper

import net.facetz.mailru.ConfigHolder
import net.facetz.mailru.api.MailRuApiProvider

trait MailRuApiConfigProvider {
  this: MailRuApiProvider =>

  private val config = ConfigHolder.config


  override protected val apiUrl  = config.apiUrl
  override protected val clientId = config.clientId
  override protected val clientSecret = config.clientSecret
}