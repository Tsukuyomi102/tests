def call(){
  sh("""#!/bin/bash
  inventoryDir="inventory"
  confDir="conf"
  src="\${confDir}/${env.ENVIR}/globalConf.conf"
  dst="\${confDir}/${env.ENVIR}/globalInventory"
  { printf '[all:vars]\n'; cat "\${src}"; } > "\${dst}"
  """)
}
return this