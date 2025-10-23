// jenkins/find_kafka_clusters_bash.groovy
def call() {
  String out = sh(
    script: '''#!/bin/bash
yq e -o=tsv - conf/distrib.yml <<'YQ' | sort -u
..
| select(tag == "!!map")
| to_entries[]
| select(.value | tag == "!!str")
| select(.value | contains("kafka_fp"))
| select(.value | test("\\.json$"))
| .key
YQ
''',
    returnStdout: true
  ).trim()

  def kafka_clusters = out ? out.readLines() : []
  echo "Kafka clusters (yq): ${kafka_clusters}"
  return kafka_clusters
}
return this
