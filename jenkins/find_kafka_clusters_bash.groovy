// jenkins/find_kafka_clusters_bash.groovy
def call() {
  String out = sh(
    script: '''#!/bin/bash
set -euo pipefail

yq -o=tsv e '
  paths(.. | scalars
    | select(type == "!!str" and contains("kafka_fp") and test("\\.json$"))
  )
  | .[-1]
  | select(type == "!!str")
' conf/distrib.yml | sort -u
''',
    returnStdout: true
  ).trim()

  def kafka_clusters = out ? out.readLines() : []
  echo "Kafka clusters (yq): ${kafka_clusters}"
  return kafka_clusters
}
return this
