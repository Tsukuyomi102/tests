// jenkins/create_two_files.groovy
// Пишет:
//  - conf/globalConf   (YAML с метаданными билда + параметрами)
//  - inventory/globalInventory (как раньше)
// Sandbox-safe: только pipeline steps (dir, writeFile, readFile, echo, pwd, env)

def call(Map cfg = [:]) {
  def ws        = pwd()
  def now       = new Date().format("yyyy-MM-dd'T'HH:mm:ssXXX")
  def buildTag  = env.BUILD_TAG     ?: ''
  def buildNum  = env.BUILD_NUMBER  ?: ''
  def nodeName  = env.NODE_NAME     ?: ''
  def branch    = env.BRANCH_NAME   ?: (env.GIT_BRANCH ?: '')
  def commit    = env.GIT_COMMIT    ?: ''
  def jobName   = env.JOB_NAME      ?: ''
  def repoUrl   = env.GIT_URL       ?: ''

  // Пользовательские значения из cfg (с дефолтами)
  def appEnv    = cfg.get('app_env', 'dev')
  def apiUrl    = cfg.get('api_url', 'https://api.example.com')
  def featureX  = cfg.get('feature_x_enabled', false) as boolean
  def extraText = (cfg.get('extra', '') ?: '').toString()
  def overwrite = !(cfg.get('append', false) as boolean) // true = перезаписать

  // 1) conf/globalConf (YAML)
  dir('conf/allConf') {
    def path = "${pwd()}/conf/allConf/globalConf"

    // YAML контент
    def yaml = """# globalConf
meta:
  job: "${jobName}"
  build_tag: "${buildTag}"
  build_number: "${buildNum}"
  node: "${nodeName}"
  branch: "${branch}"
  commit: "${commit}"
  repository: "${repoUrl}"
  workspace: "${ws}"
  timestamp: "${now}"
config:
  app_env: "${appEnv}"
  api_url: "${apiUrl}"
  feature_x_enabled: ${featureX}
"""

    if (extraText) {
      yaml += """  extra: |-
${extraText.split('\\r?\\n').collect { '    ' + it }.join('\n')}
"""
    }

    if (overwrite) {
      writeFile file: 'globalConf.conf', text: yaml, encoding: 'UTF-8'
    } else {
      // append (read + concat + write)
      def existing = ''
      try { existing = readFile file: 'globalConf.conf', encoding: 'UTF-8' } catch (ignored) {}
      writeFile file: 'globalConf.conf', text: (existing + (existing ? "\n" : "") + yaml), encoding: 'UTF-8'
    }
    echo "Wrote conf/globalConf.conf"
  }
}
return this
