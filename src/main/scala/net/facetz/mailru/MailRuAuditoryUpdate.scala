package net.facetz.mailru

import java.text.SimpleDateFormat
import java.util.Date

import net.facetz.mailru.helper.SimpleLogger
import net.facetz.mailru.directory.MailRuAuditoryUpdater
import net.facetz.mailru.helper.MailRuApiConfigProvider

@Deprecated //atwork
object MailRuAuditoryUpdate extends SimpleLogger {

  private val date = new SimpleDateFormat("yyyyMMdd").format(new Date())

  def run(): Unit = {
    val auditoryUpdater = new MailRuAuditoryUpdater with MailRuApiConfigProvider {
      override protected val dateStr = date
    }

    auditoryUpdater.run()
  }

}
