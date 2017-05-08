#!groovy

def update(def req, String stashName) {

    def params = ""
    def tags = ""

    for (param in req.parameters) {
        params = "${params}ParameterKey=${param.key},ParameterValue=\\\"${param.value}\\\" "
    }

    for (tag in req.tags) {
        tags = "${tags}Key=\"${tag.key}\",Value=\"${tag.value}\" "
    }

    def script = """#!/bin/bash

        echo 'Validate Cloudformation Template'
        aws cloudformation validate-template \\
            --template-url https://s3.amazonaws.com/${req.bucket}/jobs/${req.s3JobName}/${env.BUILD_NUMBER}/${req.cftPath} \\
            --region ${req.region} \\
            --profile ${req.profile}

        echo 'Check if stack exists'
        aws cloudformation describe-stacks \\
            --stack-name ${req.stackName} \\
            --region ${req.region} \\
            --profile ${req.profile}

        if [ "\$?" -eq "0" ]; then
            echo 'Updating Stack'
            aws cloudformation update-stack \\
                --stack-name ${req.stackName} \\
                --template-url https://s3.amazonaws.com/${req.bucket}/jobs/${req.s3JobName}/${env.BUILD_NUMBER}/${req.cftPath} \\
                --capabilities CAPABILITY_IAM \\
                --parameters ${params} \\
                --tags ${tags} \\
                --region ${req.region} \\
                --profile ${req.profile} &> tempUpdateStackOut

            tmpErrorLevel=\$?
            cat tempUpdateStackOut

            grep 'No updates are to be performed' tempUpdateStackOut
            if [ "\$?" -eq "0" ]; then
            echo "Skipping Update"
            else
                if [ "\$tmpErrorLevel" -ne "0" ]; then
                    FAIL
                else
                    aws cloudformation wait stack-update-complete \\
                        --stack-name ${req.stackName} \\
                        --region ${req.region} \\
                        --profile ${req.profile}
                fi
            fi
        else
            echo 'Creating Stack'

            aws cloudformation create-stack  \\
                --stack-name ${req.stackName} \\
                --template-url https://s3.amazonaws.com/${req.bucket}/jobs/${req.s3JobName}/${env.BUILD_NUMBER}/${req.cftPath} \\
                --capabilities CAPABILITY_IAM \\
                --parameters ${params} \\
                --tags ${tags} \\
                --region ${req.region} \\
                --profile ${req.profile}

            aws cloudformation wait stack-create-complete \\
                --stack-name ${req.stackName} \\
                --region ${req.region} \\
                --profile ${req.profile}
        fi

        if [ "\$?" -ne "0" ]; then
            echo "Stack creation/update failed"
            exit 1
        fi
        echo 'Storing Stack Output'
        aws cloudformation describe-stacks \\
            --stack-name ${req.stackName} \\
            --region ${req.region} \\
            --profile ${req.profile} \\
            --output json \\
            --query 'Stacks[0].Outputs' > cft-stack-outputs.json
    """

    writeFile file: "cft-infra.sh", text: script
    sh "chmod +x cft-infra.sh && ./cft-infra.sh"
    stash name: stashName, includes: "cft-stack-outputs.json"
}

return this;