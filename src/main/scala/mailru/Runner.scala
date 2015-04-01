package mailru

import java.text.SimpleDateFormat
import java.util.Date

import net.facetz.export.mr.mailru.MailRuExport

object Runner {

  val today = new SimpleDateFormat("yyyyMMdd").format(new Date())

  case class Config(workingDirectory: String = "",
                   outputFolderName: String = "results",
                   regexp: String = ".*[.gz]",
                   dateStr: String = today,
                   clientId: String = "",
                   clientSecret: String = "",
                   upload: Boolean = true,
                   partnerPrefix: String = ""
                     )

  //args example: -i /tmp/gz -u someclientid -s somesecret -p dl
  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[Config]("mailru-segment-exporter") {
      head("mailru-segment-exporter")
      opt[String]('i', "workdir")
        .valueName("<workdir>")
        .action({ (value, config) => config.copy(workingDirectory = value)})
        .text("ex. /opt/segments")
        .required() //TODO get from workingdirectory by default
      opt[String]('u', "client")
        .valueName("<client>")
        .action({ (value, config) => config.copy(clientId = value)})
        .text("you client id from mailru")
        .required()
      opt[String]('s', "secret")
        .valueName("<secret>")
        .action({ (value, config) => config.copy(clientSecret = value)})
        .text("you client secret from mailru")
        .required()
      opt[String]('p', "prefix")
        .valueName("<prefix>")
        .action({ (value, config) => config.copy(partnerPrefix = value)})
        .text("mailru partner prefix")
        .required()
      opt[String]('d', "date")
        .valueName("<date>")
        .action({ (value, config) => config.copy(dateStr = value)})
        .text("ex. 20151231, now by default")
      opt[String]('r', "regexp")
        .valueName("<regexp>")
        .action({ (value, config) => config.copy(regexp = value)})
        .text("filename pattern, ex.    .*[.gz]")
      opt[Boolean]('u', "upload")
        .valueName("<upload>")
        .action({ (value, config) => config.copy(upload = value)})
        .text("upload to mailRu or not. true by default")
      opt[String]('o', "outputname")
        .valueName("<outputname>")
        .action({ (value, config) => config.copy(outputFolderName = value)})
        .text("output folder name, 'results' by default")
    }

    //TODO validate if upload and client&secret empty
    parser.parse(args, Config()).map { config =>
      ConfigHolder.config = config
      MailRuExport.run
      sys.exit()
    }.getOrElse {
      sys.error("cmdline args are wrong; please rerun with '--help' flag to see the usage example")
      sys.exit(1)
    }
  }

}
