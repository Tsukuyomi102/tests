// jenkins/find_kafka_clusters_bash.groovy
def call() {
  String out = sh(
    script: '''#!/bin/bash
set -euo pipefail

yq e -o=tsv '
  ..                          # обойти все узлы
  | select(tag == "!!map")    # оставить только мапы
  | to_entries[]              # пары {key, value}
  | select(.value | tag == "!!str")         # строго строки
  | select(.value | contains("kafka_fp"))   # содержит kafka_fp
  | select(.value | test("\\\\.json$"))     # заканчивается на .json
  | .key                      # вывести имя ключа
' conf/distrib.yml | sort -u
''',
    returnStdout: true
  ).trim()

  def kafka_clusters = out ? out.readLines() : []
  echo "Kafka clusters (yq): ${kafka_clusters}"
  return kafka_clusters
}
return this
