pipeline {

	agent any

	environment {
	    release_type = ${GIT_LOCAL_BRANCH == 'master'? 'final' : 'candidate'}

	}


	stages {

		stage('test') {

			steps {
				echo "GIT_LOCAL_BRANCH: $GIT_LOCAL_BRANCH"
			}

		}

	}

}