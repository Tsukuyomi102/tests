// jenkins/find_kafka_clusters_bash.groovy
def call() {
  String out = sh(
    script: '''#!/bin/bash
set -euo pipefail

yq e -o=tsv '
  ..                                                     # пройти по всем нодам
  | select(tag == "!!map")                               # оставить только мапы
  | to_entries[]                                         # развернуть в пары {key, value}
  | select( .value
            | tag == "!!str"
            and contains("kafka_fp")
            and test("\\.json$") )                       # фильтр значения
  | .key                                                 # вывести имя ключа
' conf/distrib.yml | sort -u
''',
    returnStdout: true
  ).trim()

  def kafka_clusters = out ? out.readLines() : []
  echo "Kafka clusters (yq): ${kafka_clusters}"
  return kafka_clusters
}
return this
