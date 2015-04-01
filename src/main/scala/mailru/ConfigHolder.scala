package mailru

import mailru.Runner.Config

object ConfigHolder {
  
  var config: Config = null

  def getConfiguration = config

}
