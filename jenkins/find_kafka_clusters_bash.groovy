// jenkins/find_kafka_clusters_bash.groovy
def call() {
  String out = sh(
    script: '''#!/bin/bash
yq e -o=tsv '
..
| select(tag == "!!map")
| to_entries | .[]
| select(.value | tag == "!!str")
| select(.value | contains("kafka_fp"))
| select(.value | test("\\.json\$"))
| .key
' conf/distrib.yml | sort -u
''',
    returnStdout: true
  ).trim()
  echo "out: ${out}"
  def kafka_clusters = out ? out.readLines() : []
  echo "Kafka clusters (yq): ${kafka_clusters}"
  return kafka_clusters
}
return this
