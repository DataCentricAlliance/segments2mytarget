package net.facetz.mailru.helper

trait SimpleLogger {
  protected val log = new Log()
}

class Log {
  def info(msg: String): Unit = {
    println(msg)
  }

  def error(msg: String): Unit = {
    sys.error(msg)
  }

  def error(msg: String, t: Throwable): Unit = {
    sys.error(msg)
    t.printStackTrace()
  }


}