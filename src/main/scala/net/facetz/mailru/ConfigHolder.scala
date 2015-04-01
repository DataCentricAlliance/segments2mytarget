package net.facetz.mailru

import net.facetz.mailru.Runner.Config

object ConfigHolder {
  
  var config: Config = null

  def getConfiguration = config

}
