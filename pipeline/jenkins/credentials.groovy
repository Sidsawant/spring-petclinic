#!groovy

def getSecretText(String credentialsId) {
    def value
    withCredentials([string(credentialsId: credentialsId, variable: "SecretText")]) {
        value = "${env.SecretText}"
    }
    return value
}

return this;
