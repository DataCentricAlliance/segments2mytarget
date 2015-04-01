package net.facetz.mailru

import java.text.SimpleDateFormat
import java.util.Date

object Runner {

  val today = new SimpleDateFormat("yyyyMMdd").format(new Date())

  case class Config(workingDirectory: String = "",
                    partnerPrefix: String = "",
                    outputFolderName: String = "results",
                    dateStr: String = today,
                    regexp: String = ".*[.gz]",
                    upload: Boolean = false,
                    clientId: String = "",
                    clientSecret: String = ""
                     )

  //args example: -i /tmp/gz -c someclient -s somesecret -p dl -u
  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[Config]("mailru-segment-exporter") {
      head("mailru-segment-exporter")
      opt[String]('i', "workdir")
        .valueName("<workdir>")
        .action({ (value, config) => config.copy(workingDirectory = value)})
        .text("ex. /opt/segments")
        .required()
      opt[String]('p', "prefix")
        .valueName("<prefix>")
        .action({ (value, config) => config.copy(partnerPrefix = value)})
        .text("mailru partner prefix")
        .required()
      opt[String]('o', "outputname")
        .valueName("<outputname>")
        .action({ (value, config) => config.copy(outputFolderName = value)})
        .text("output folder name, 'results' by default")
      opt[String]('d', "date")
        .valueName("<date>")
        .action({ (value, config) => config.copy(dateStr = value)})
        .text("ex. 20151231, now by default")
      opt[String]('r', "regexp")
        .valueName("<regexp>")
        .action({ (value, config) => config.copy(regexp = value)})
        .text("filename pattern, ex.    .*[.gz]")

      opt[Unit]('u', "upload")
        .valueName("<upload>")
        .action({ (_, config) => config.copy(upload = true)})
        .text("upload to mailRu or not. true by default")
      opt[String]('c', "client")
        .valueName("<client>")
        .action({ (value, config) => config.copy(clientId = value)})
        .text("you client id from mailru")
      opt[String]('s', "secret")
        .valueName("<secret>")
        .action({ (value, config) => config.copy(clientSecret = value)})
        .text("you client secret from mailru")

      checkConfig(c => if (c.upload && (c.clientId.isEmpty || c.clientSecret.isEmpty)) {
        failure("you want to upload but not set clientId or clientSecret")
      } else {
        success
      })
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        ConfigHolder.config = config
        MailRuExport.run
        sys.exit()
      case None =>
        sys.error("cmdline args are wrong; please rerun with '--help' flag to see the usage example")
        sys.exit(1)
    }

  }

}
