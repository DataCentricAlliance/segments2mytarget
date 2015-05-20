package net.facetz.mailru.helper

import java.text.SimpleDateFormat
import java.util.Date

trait SimpleLogger {
  protected val log = new Log()
}

class Log {

  val dateFormat = new ThreadLocal[SimpleDateFormat] {
    override def initialValue(): SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  }

  def log(msg: String): Unit = {
    println(s"${dateFormat.get().format(new Date())}: $msg")
  }

  def info(msg: String): Unit = {
    log(s"INFO: $msg")
  }

  def error(msg: String): Unit = {
    log(s"ERROR: $msg")
  }

  def error(msg: String, t: Throwable): Unit = {
    error(msg)
    t.printStackTrace()
  }
}
