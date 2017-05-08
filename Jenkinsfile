#!groovy

def infra = new InfraModel()
def flags = new BranchFlags("${env.BRANCH_NAME}")


def context = [
//        node                : "linux1",
        branchName          : flags._branchName,
        application         : "smartdata",
        applicationVersion  : "",
        version             : "${flags._branchName}-${env.BUILD_NUMBER}",
        bucket              : flags.isReleasableBranch() ? "sterling-artifacts-us-east-1" : "sterling-artifacts-dev-us-east-1",
        certificatesBucket  : flags.isReleasableBranch() ? "sterling-certificates" : "sterling-certificates-dev",
        certSuffix          : 'backgroundcheck',
        domain              : 'backgroundcheck.com',
        // authDomain          : 'sterlingbackcheck.auth0.com',
        profile             : flags.isReleasableBranch() ? "sterling-prod" : "sterling-dev",
        uuid                : flags.isHotfixOrFeature() ? getBranchUuid(flags._branchName) : "",
        region              : "us-east-1",
        awsAccount          : flags.isReleasableBranch() ? "prod" : "dev",
        awsAccountNumber    : flags.isReleasableBranch() ? "231469678781" : "882038671278",
        authorizerName      : flags.isReleasableBranch() ? "authapi-gateway-authorizer-v2" : "authapi-gateway-authorizer",
        isUnique            : flags.isHotfixOrFeature() ? "yes" : "no",
        errorPolicy         : flags.isReleasableBranch() ? "Never" : "Always",
        sslFlags            : "Ssl",
        loggingLevel        : flags.isReleasableBranch() ? "Info" : "Trace",
        s3JobName           : "${env.JOB_NAME}".replaceAll('%2F', '%252F'),
        jenkinsWorkspace    : "${env.JOB_NAME}".replaceAll('%2F', '-'),
        keyPair             : flags.isReleasableBranch() ? "api-services" : "api-services-dev"
]

println "Pipeline Version='${context.version}'"
println "Environment='${env}'"
println "Branch name='${env.BRANCH_NAME}'"
println "Job name='${env.JOB_NAME}'"
println "S3 Job name='${context.s3JobName}'"
println "Build number='${env.BUILD_NUMBER}'"

properties([
        disableConcurrentBuilds(),
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')),
        pipelineTriggers([[$class: 'PeriodicFolderTrigger', interval: '1m']])
])


    node(context.node) {
        ws("${context.jenkinsWorkspace}") {
            try {
			
                // def gitVersionOutput = null
				echo("test")
                stage("SCM") {
                    checkout scm
                    // gitVersionOutput = getGitVersionOutput()
                    // context.applicationVersion = "${gitVersionOutput.GitVersion_NuGetVersionV2}"
                    // echo("context.applicationVersion: ${context.applicationVersion}")

                    //def inputData = 	('Jenkinsfile.UnsecuredSettings.json')
                   // context.settings = parseJson(inputData)
                }

                //def s3 = load("pipeline/aws/s3.groovy")
                def maven = load("pipeline/java/maven.groovy")
				stage("Build") {
				echo('in build')
                  maven.build([
                    
                    ])
				}

                stage("Unit Tests") {
                }

                stage("Code Analytics") {
                }

                stage("Package") {
                }

                stage("Publish Artifacts") {
                    s3.publish([
                            bucket : context.bucket, region: context.region, account: context.awsAccount,
                            entries: [
                                    [flatten: true, file: "main_output/${context.main.artifactName}"],
                                    [flatten: true, file: "receiver_output/${context.receiver.artifactName}"],
                                    [flatten: true, file: "connector_output/${context.connector.artifactName}"],
                                    [flatten: false, file: "infra/"]
                            ]
                    ])

                    stash name: "${context.application}-${context.branchName}"
                }
            }
            finally {
                step([$class: 'WsCleanup', notFailBuild: true])
            }
        }
    }


@NonCPS
def parseJson(inputData) {
    def jsonSlurper = new groovy.json.JsonSlurperClassic()
    def jsonData = jsonSlurper.parseText(inputData)

    return jsonData
}

@NonCPS
def getSetting(settings, settingName, environment) {
    def settingNameCap = settingName.capitalize()

    if (settings[settingNameCap] != null) {
        def envSettings = settings[settingNameCap]
        def environmentCap = environment.capitalize()

        if (envSettings[environmentCap] != null) {
            envSettings[environmentCap]
        } else if (envSettings["Default"] != null) {
            return envSettings["Default"]
        } else {
            return null
        }
    }
}

def echo(String message) {
    bat "echo ${message}"
}

String getBranchUuid(String branchName) {
    def index = branchName.indexOf('-')
    return index >= 0 ? branchName.substring(index + 1) : branchName
}

@NonCPS
Object loadEnv(Object text) {
    def e = new hudson.EnvVars()
    text.eachLine {
        line ->
            def keyValue = line.split('=')
            def key = keyValue[0]

            if (keyValue.size() > 1) {
                value = keyValue[1]
            } else {
                value = ""
            }
            e.put(key, value)
    }

    return e
}

class BranchFlags implements Serializable {
    private String _branchName

    BranchFlags(branch) {
        this._branchName = branch.replaceAll('/', '-')
    }

    boolean isMasterBranch() {
        this._branchName == "master"
    }

    boolean isHotfix() {
        this._branchName.startsWith('hf') || this._branchName.startsWith('hotfix-')
    }

    boolean isFeature() {
        this._branchName.startsWith('fb-') || this._branchName.startsWith('feature-')
    }

    boolean isDevelopBranch() {
        this._branchName.contains('develop')
    }

    boolean isReleaseBranch() {
        this._branchName.contains('release')
    }

    boolean isHotfixOrFeature() {
        this.isHotfix() || this.isFeature()
    }

    boolean isReleasableBranch() {
        this.isHotfix() || this.isReleaseBranch()
    }
}

class InfraModel implements Serializable {

    String environment
    String properEnvironment
    String stage
    String basePath
    String dbStack
    String ebStackName
    String ebEnvironment

    def reset(context, environment, properEnvironment) {
        this.environment = environment
        this.properEnvironment = properEnvironment
        this.dbStack = "rds-api-shared-aurora-${environment}"

        switch (environment) {
            case "pre":
            case "int":
                this.stage = "${environment}v1"
                this.basePath = 'v1'
                break;
            case "prod":
                this.stage = "${environment}v1"
                this.basePath = 'v1'
                this.dbStack = "rds-${context.application}-aurora-${environment}"
                break;
            default:
                this.stage = this.cleanBranchName(context)
                this.basePath = this.cleanBranchName(context)
                break;
        }

        this.ebStackName = "eb-${context.application}-${environment}"
        this.ebEnvironment = "${environment}"
        if (context.uuid != "") {
            this.ebStackName = "eb-${context.application}-${environment}-${context.uuid}"
            this.ebEnvironment = "${environment}-${context.uuid}"
        }
    }

    def cleanBranchName(context) {
        def output

        if (context.branchName.contains('develop')) {
            output = "dev"
        }
        else {
            output = context.uuid.replaceAll('-', '')
        }

        return output
    }
}