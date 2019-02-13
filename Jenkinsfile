pipeline {

	agent any

	stages {

		stage('test') {

			steps {
				sh 'printenv'
				echo "--"
				for (c in com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class, Jenkins.instance, null, null)) {
					 println(c.id + ": " + c.description)
				}
			}

		}

	}

}