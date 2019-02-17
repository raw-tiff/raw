pipeline {

	agent { label 'jenkins-slave-01' }

	environment {
		GITHUB_TOKEN = credentials("GITHUB_TOKEN")
		BINTRAY_API_KEY = credentials("BINTRAY_API_KEY")
		RELEASE_TYPE = "${GIT_LOCAL_BRANCH == 'master'? 'final' : 'candidate'}"
	}

	stages {
		stage('publish') {
			steps {
				sh "./gradlew " +
						"clean " +
						"-Dorg.ajoberstar.grgit.auth.password=$GITHUB_TOKEN $RELEASE_TYPE " +
						"-PbintrayApiKey=$BINTRAY_API_KEY bintrayUpload"
			}
		}
	}

}