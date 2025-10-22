pipeline {
  agent any
  options { timestamps() }
  environment {
    ENVIR = 'allConf'
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Create files via helper') {
      steps {
        script {
          // Подгружаем groovy-скрипт из репозитория и запускаем
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
