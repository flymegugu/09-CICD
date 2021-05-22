pipeline {
	agent any //提供代理执行流水线
	environment {
		//设置整个流水线的环境变量
	}
	stages {
		stage('步骤名') {
			when {
				//条件判断	
			}
			steps{
				//步骤具体执行
			}
		}
	}
	post{
		
	}
}
