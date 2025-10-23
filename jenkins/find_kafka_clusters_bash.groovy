// jenkins/find_kafka_clusters_bash.groovy
def call() {
  String out = sh(
    script: '''#!/bin/bash
set -euo pipefail

yq e -o=tsv '
  ..
  | select(tag == "!!map")
  | to_entries[]
  | select(.value | tag == "!!str")
  | select(.value | contains("kafka_fp"))
  | select(.value | endswith(".json"))
  | .key
' conf/distrib.yml | sort -u
''',
    returnStdout: true
  ).trim()

  def kafka_clusters = out ? out.readLines() : []
  kafka_clusters = kafka_clusters.unique()
  echo "Kafka clusters (yq): ${kafka_clusters}"
  return kafka_clusters
}
return this
