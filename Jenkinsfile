pipeline {

	agent any

	stages {

		stage('test') {

			steps {
				sh 'printenv'
				echo "--"
				creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
					com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class,
					Jenkins.instance,
					null,
					null
				);
				for (c in creds) {
					 println(c.id + ": " + c.description)
				}
			}

		}

	}

}