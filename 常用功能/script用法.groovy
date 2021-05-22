pipeline {
    agent any
    stages {
        stage('Hello') {
            steps {
                script {
                    result = sh (script: "kubectl get po -n openstack|grep deploy-serve|grep -v Running", returnStatus: true) 
                    echo "result: ${result}"
                }
            }
        }
    }
}
