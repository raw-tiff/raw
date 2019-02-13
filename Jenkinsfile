pipeline {

	agent any

	stages {

		stage('test') {

			steps {
				echo "${env.CHANGE_ID}"
				echo "${env.CHANGE_URL}"
				echo "${env.CHANGE_TITLE}"
				echo "${env.CHANGE_AUTHOR}"
				echo "${env.CHANGE_AUTHOR_DISPLAY_NAME}"
				echo "${env.CHANGE_AUTHOR_EMAIL}"
				echo "${env.CHANGE_TARGET}"
				echo "--"
				sh 'printenv'
			}

		}

	}

}