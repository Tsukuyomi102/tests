pipeline {
  agent any
  options { timestamps() }
  environment {
    ENVIR = 'allConf'
    YQ_VERSION = 'v4.20.4'          // целевая версия
    YQ_FALLBACK_VERSION = 'v4.20.2' // фолбэк, если 4.20.4 недоступна
    PATH = "${WORKSPACE}/.tools:${PATH}"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Install yq') {
      steps {
        sh '''#!/bin/bash
set -euo pipefail
mkdir -p .tools

want_ver="${YQ_VERSION}"
fallback_ver="${YQ_FALLBACK_VERSION}"

# уже нужная версия?
if [ -x .tools/yq ] && .tools/yq --version 2>/dev/null | grep -q "version ${want_ver#v}"; then
  .tools/yq --version
  exit 0
fi

case "$(uname -s)" in
  Linux)  os=linux ;;
  Darwin) os=darwin ;;
  *) echo "Unsupported OS: $(uname -s)" >&2; exit 1 ;;
esac

case "$(uname -m)" in
  x86_64)        arch=amd64 ;;
  aarch64|arm64) arch=arm64 ;;
  *) echo "Unsupported arch: $(uname -m)" >&2; exit 1 ;;
esac

download_yq() {
  local ver="$1"
  local url="https://github.com/mikefarah/yq/releases/download/${ver}/yq_${os}_${arch}"
  echo "Trying yq ${ver} from: $url"
  local tmp=".tools/.yq.tmp"
  rm -f "$tmp"
  if command -v curl >/dev/null 2>&1; then
    http=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    if [ "$http" != "200" ]; then
      echo "HTTP $http for ${url}"
      return 1
    fi
    curl -fsSL "$url" -o "$tmp"
  else
    wget -qO "$tmp" "$url" || return 1
  fi
  chmod +x "$tmp"
  mv -f "$tmp" .tools/yq
}

# пробуем целевую, если не получилось — фолбэк
if ! download_yq "$want_ver"; then
  echo "Falling back to ${fallback_ver}…"
  download_yq "$fallback_ver"
fi

.tools/yq --version
'''
      }
    }

    stage('Create files via helper') {
      steps {
        script {
          def helper = load 'jenkins/create_two_files.groovy'
          helper.call()
        }
      }
    }

    stage('Copy paste via helper') {
      steps {
        script {
          def helper = load 'jenkins/copy.groovy'
          helper.call()
        }
      }
    }

    stage('Create distrib.yml') {
      steps {
        script {
          dir('conf') {
            writeFile file: 'distrib.yml', text: '''
any_apps:
  debug:
    some_value: value
    dasdadacxzadasda: "kafka_fp_fdsla;dfs;ksflk;sdf;dkl;fdsklf;ksfl;dsfkl;.json"
something_else:
  debug_2:
    some_value_2: value2

all_clusters:
  dsacxzzx: "kafka_fp_path.json"
  dasdcxz: "kafka_fp_path2.json"
only_2_cluster:
  dasdcxz: "kafka_fp_path2.json"
third_added_cluster:
  fiofgdosijoijg: "kafka_fp_path3.json"
fouth_added_cluster:
  cluster4: "nginx_fp_path.json"
fifth_added_cluster:
  cluster5: "kafka_fp_path3.txt"
'''
          }
        }
      }
    }

    stage('Find kafka clusters (bash helper)') {
      steps {
        script {
          def helper = load 'jenkins/find_kafka_clusters_bash.groovy'
          def kafka_clusters = helper.call()
          echo "Result clusters: ${kafka_clusters}"
        }
      }
    }
  }

  post {
    always {
      // чтобы сразу видеть результат в артефактах
      archiveArtifacts artifacts: 'conf/**, inventory/**', allowEmptyArchive: true
    }
  }
}
