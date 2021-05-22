#自动终止
pipeline {
    agent any
    environment {
        WORKDIR = '/var/lib/jenkins/workspace/openstack-deploy-server'
        DeployServer = 'openstack-deploy-server-0.1.0.tgz'
    }
    stages {
        stage('设置node标签') {
            steps {
                sleep 5
                timeout(activity: true, time: 5, unit: 'SECONDS') {
                    sh ' kubectl logs -f "$(kubectl get po -n openstack|grep deploy-server|awk -F\' \' \'{print $1}\')" -n openstack'
                }
            }
           
        }
    }
}