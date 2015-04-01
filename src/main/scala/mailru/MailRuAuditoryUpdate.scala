package net.facetz.export.mr.mailru

import java.text.SimpleDateFormat
import java.util.Date

import mailru.helper.SimpleLogger
import net.facetz.export.mr.mailru.directory.MailRuAuditoryUpdater
import net.facetz.export.mr.mailru.helper.MailRuApiConfigProvider

@Deprecated("atwork")
object MailRuAuditoryUpdate extends App with SimpleLogger {

  private val date = new SimpleDateFormat("yyyyMMdd").format(new Date())

  def run(): Unit = {
    val auditoryUpdater = new MailRuAuditoryUpdater with MailRuApiConfigProvider {
      override protected val dateStr = date
    }

    auditoryUpdater.run()
  }

}
