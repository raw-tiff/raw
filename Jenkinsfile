pipeline {

	agent any

	environment {
		GITHUB_ID = credentials("GITHUB_ID")
		GITHUB_TOKEN = credentials("GITHUB_TOKEN")
		//BINTRAY_API_KEY = credentials("BINTRAY_API_KEY")
		RELEASE_TYPE = "${GIT_LOCAL_BRANCH == 'master'? 'final' : 'candidate'}"
	}

	stages {

		stage('publish') {

			steps {
				//sh "./gradlew clean $RELEASE_TYPE bintrayUpload -PbintrayApiKey=$BINTRAY_API_KEY"
				sh "./gradlew -Dorg.ajoberstar.grgit.auth.username=$GITHUB_ID -Dorg.ajoberstar.grgit.auth.password=$GITHUB_TOKEN --info clean $RELEASE_TYPE"
			}

		}

	}

}