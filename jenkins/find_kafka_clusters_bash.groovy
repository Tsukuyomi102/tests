def call() {
  final String DISTRIB_PATH = 'conf/distrib.yml'

  // Валидация YAML (упадёт, если синтаксис битый)
  def _ = readYaml(file: DISTRIB_PATH)

  // Ищем кластеры через yq
  String out = sh(
    script: '''#!/bin/bash
set -euo pipefail

DISTRIB_PATH="conf/distrib.yml"

# Найти yq или установить локально (mikefarah/yq v4)
if command -v yq >/dev/null 2>&1; then
  YQ_CMD="$(command -v yq)"
else
  mkdir -p .tools
  YQ_CMD=".tools/yq"
  if [ ! -x "$YQ_CMD" ]; then
    arch="$(uname -m)"
    case "$arch" in
      x86_64)  suffix="amd64" ;;
      aarch64|arm64) suffix="arm64" ;;
      *) echo "Unsupported arch: $arch" >&2; exit 1 ;;
    esac
    url="https://github.com/mikefarah/yq/releases/download/v4.44.3/yq_linux_${suffix}"
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "$url" -o "$YQ_CMD"
    else
      wget -qO "$YQ_CMD" "$url"
    fi
    chmod +x "$YQ_CMD"
  fi
fi

# Ищем ВСЕ строковые скаляры, где есть 'kafka_fp' и окончание '.json',
# и печатаем ПОСЛЕДНИЙ элемент пути (имя ключа). Исключаем индексы массивов.
"$YQ_CMD" -o=tsv e '
  paths(.. | scalars | select(type == "!!str" and contains("kafka_fp") and test("\\.json$"))) |
  .[-1] | select(type == "!!str")
' "$DISTRIB_PATH" | sort -u
''',
    returnStdout: true
  ).trim()

  def kafka_clusters = out ? out.readLines().findAll { it?.trim() } : []
  kafka_clusters = kafka_clusters.unique()

  echo "Kafka clusters (yq): ${kafka_clusters}"
  return kafka_clusters
}
return this
