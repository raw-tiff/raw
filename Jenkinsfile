pipeline {

	agent any

	environment {
		//BINTRAY_API_KEY = credentials("BINTRAY_API_KEY")
		RELEASE_TYPE = "${GIT_LOCAL_BRANCH == 'master'? 'final' : 'candidate'}"
	}

	stages {

		stage('publish') {

			steps {
				//sh "./gradlew clean $RELEASE_TYPE bintrayUpload -PbintrayApiKey=$BINTRAY_API_KEY"
				sh "./gradlew --info clean $RELEASE_TYPE"
			}

		}

	}

}