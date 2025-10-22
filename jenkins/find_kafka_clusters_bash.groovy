def call() {
  final String DISTRIB_PATH = 'conf/distrib.yml'

  // Валидируем YAML (если битый — упадёт тут)
  def _ = readYaml(file: DISTRIB_PATH)

  // Берём сырой текст YAML
  String yamlText = readFile(DISTRIB_PATH)

  // Как просил — переменная остаётся в Groovy
  def kafka_clusters = []

  // ВЕСЬ парсинг — одной awk-командой; без grep, без нестабильных кодов возврата
  def out = sh(
    script: '''#!/bin/bash
set -euo pipefail

awk '
  /^[[:space:]]*#/ { next }
  {
    line = $0
    c = index(line, ":")
    if (c == 0) next

    key  = substr(line, 1, c-1)
    rest = substr(line, c+1)

    # trim key/rest
    gsub(/^[ \\t]+|[ \\t]+$/, "", key)
    gsub(/^[ \\t]+|[ \\t]+$/, "", rest)

    # ждём строго строку вида: key: "value"
    if (rest ~ /^"[^"]*"$/) 
    {
      val = rest
      sub(/^"/, "", val)
      sub(/"$/, "", val)

      # правило: содержит kafka_fp и заканчивается на .json (без регэкспов)
      if (index(val, "kafka_fp") > 0 && substr(val, length(val)-4) == ".json") 
      {
        print key
      }
    }
  }
' <<'__YAML__' | sort -u
''' + yamlText + '''
__YAML__
''',
    returnStdout: true
  ).trim()

  if (out) {
    kafka_clusters = out.readLines().findAll { it?.trim() }
  }

  // Оставляем финальную уникализацию и вывод
  kafka_clusters = kafka_clusters.unique()
  kafka_clusters = kafka_clusters.unique()
  echo "Kafka clusters: ${kafka_clusters}"
  return kafka_clusters
}
return this
