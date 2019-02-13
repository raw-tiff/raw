pipeline {

	agent any

	stages {

		stage('test') {

			steps {
				echo "${CHANGE_ID}"
				echo "${CHANGE_URL}"
				echo "${CHANGE_TITLE}"
				echo "${CHANGE_AUTHOR}"
				echo "${CHANGE_AUTHOR_DISPLAY_NAME}"
				echo "${CHANGE_AUTHOR_EMAIL}"
				echo "${CHANGE_TARGET}"
				echo "--"
				sh 'printenv'
			}

		}

	}

}