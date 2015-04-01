package net.facetz.mailru.helper

trait SimpleLogger {
  protected val log = new Log()
}

class Log {

  def log(msg: String): Unit = {
    println(msg)
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