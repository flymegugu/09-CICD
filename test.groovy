pipeline {
    agent any
    environment {
        BUILD_USER = ''
    }

    tools {
        maven 'maven'
        jdk   'Oracle_jdk8'
    }
    // 分支选择，注意BRANCH，与构建参数一致
    parameters {
        gitParameter branchFilter: 'origin/(.*)', defaultValue: 'master', name: 'BRANCH', type: 'PT_BRANCH'
    }
    stages {
        stage('准备') {
            steps {
                script {
                    //pipeline中的when不能直接调用参数化构建里面的参数。需要进行变量赋值。
                    ACTION = "${dev_or_test}"
                }
            }
        }
        stage('拉代码') {
            //when进行判断，若变量ACTION的值是Deploy_to_development_environment，则执行这个步骤。
            //Deploy_to_development_environment是参数化构建里面配置好的。
            when {
                equals expected: 'Deploy_to_development_environment',
              actual: ACTION
            }
            steps {
                echo '开始拉取代码.....'
                git branch: "${params.BRANCH}", credentialsId: '7XXXXXXXXf0-1de413f5ea29', url: 'http://gitlab.vonedao.com/XXXXXX.git'
            }
        }
        stage('编译打包镜像') {
            when {
                equals expected: 'Deploy_to_development_environment',
              actual: ACTION
            }
            steps {
                echo '开始执行编译打包操作.......'
                sh 'mvn clean install -Dmaven.test.skip=true -U'
                echo 'Build Docker Image'
                sh 'mvn -f ./$JOB_NAME/pom.xml  docker:stop docker:remove docker:build docker:push'
            }
        }
        stage('部署到开发环境') {
            when {
                equals expected: 'Deploy_to_development_environment',
              actual: ACTION
            }
            steps {
                echo '开始发布开发环境 .......'
                //执行自己编写的shell脚本，进行部署动作
                sh '/var/jenkins_home/piplineshell/deploy_dev.sh'
            }
        }
        stage('提交到测试') {
            //when进行判断，若变量ACTION的值是Summit_the_test，则执行这个步骤。
            //Summit_the_test是参数化构建里面配置好的。
            when {
                equals expected: 'Summit_the_test',
              actual: ACTION
            }
            steps {
                script {
                    //提测版本号不能为空
                    if (env.NewVersion) {
                        //提测版本号不能与历史提测版本号重复
                        sh '/var/jenkins_home/piplineshell/check_version_.sh'
                        //利用Jenkins的workspace下面已经拉下git的项目（开发已经部署验证通过了），进行版本号修改，然后编译打包提交给测试。
                        sh 'mvn versions:set -DnewVersion=${NewVersion} -DupdateMatchingVersions=false'
                        sh 'mvn -f ./${JOB_NAME}/pom.xml versions:update-child-modules'
                        sh 'mvn -f ./${JOB_NAME}/pom.xml clean install -Dmaven.test.skip=true -U '
                        sh 'mvn -f ./$JOB_NAME/pom.xml docker:stop docker:remove docker:build docker:push'
                        sh 'mvn -f ./$JOB_NAME/pom.xml versions:revert'
                        //该脚本作用是将本次的新版本号写入对应文件/var/jenkins_home/piplineshell/version_tag/cs-auth
                        //并且修改yaml文件中的镜像版本号，将最新yaml文件同步到测试环境。
                        sh '/var/jenkins_home/piplineshell/version_tag.sh'
                    }
                else {
                        echo 'ERROR : 没有填写提测版本号'
                        sh 'exit 1'
                }
                }
                // Get build user profile via User Build Vrs plugin
                // https://wiki.jenkins.io/display/JENKINS/Build+User+Vars+Plugin
                //企业微信消息通知
                wrap([$class: 'BuildUser']) {
                    script {
                        BUILD_USER = "${env.BUILD_USER}"
                        sh '/var/jenkins_home/piplineshell/sendmessage.sh'
                    }
                }
            }
        }
    }
}
