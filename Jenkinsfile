pipeline {
  agent any
  options { timestamps() }
  environment {
    ENVIR = 'allConf'
    YQ_VERSION = 'v4.44.3'
    // добавим локальную папку с бинарниками в PATH
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

if [ ! -x .tools/yq ]; then
  case "$(uname -s)" in
    Linux)  os=linux ;;
    Darwin) os=darwin ;;
    *) echo "Unsupported OS: $(uname -s)" >&2; exit 1 ;;
  esac

  case "$(uname -m)" in
    x86_64)           arch=amd64 ;;
    aarch64|arm64)    arch=arm64 ;;
    *) echo "Unsupported arch: $(uname -m)" >&2; exit 1 ;;
  esac

  url="https://github.com/mikefarah/yq/releases/download/${YQ_VERSION}/yq_${os}_${arch}"

  echo "Downloading yq from: $url"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$url" -o .tools/yq
  else
    wget -qO .tools/yq "$url"
  fi
  chmod +x .tools/yq
fi

# показать версию для дебага
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
      archiveArtifacts artifacts: 'conf/**, inventory/**', allowEmptyArchive: true
    }
  }
}
