#!groovy

def updateDomain(def req) {

    String jq = "jq-linux64"

    def script = """#!/bin/bash

        echo "Application='${req.application}' | AwsAccount='${req.account}'"
        echo "Certificate Bucket='${req.certBucket}'"
        echo "Cert Env='${req.environment}'"
        echo "Cert Suffix='${req.certSuffix}'"
        echo "Custom Domain='${req.domain}'"
        echo "AWS Profile='${req.profile}'"

        certSuffix="${req.certSuffix}"

        customDomain="${req.application}.${req.environment}.${req.domain}"

        echo "Checking if Custom Domain exists"
        aws apigateway get-domain-name \\
            --domain-name \$customDomain \\
            --region ${req.region} \\
            --profile ${req.profile}

        if [ "\$?" -ne "0" ]; then
            echo "Downloading SSL certificates"
            aws s3 cp s3://${req.certBucket}/${req.domain}/${req.environment}.\${certSuffix}.1.cer . \\
                --region ${req.region} \\
                --profile ${req.profile}

            if [ "\$?" -ne "0" ]; then
                echo "Certificate download failed"
                exit 1
            fi

            aws s3 cp s3://${req.certBucket}/${req.domain}/${req.environment}.\${certSuffix}.2.key . \\
                --region ${req.region} \\
                --profile ${req.profile}

            if [ "\$?" -ne "0" ]; then
                echo "Certificate download failed"
                exit 1
            fi

            aws s3 cp s3://${req.certBucket}/${req.domain}/${req.environment}.\${certSuffix}.3.cer . \\
                --region ${req.region} \\
                --profile ${req.profile}

            if [ "\$?" -ne "0" ]; then
                echo "Certificate download failed"
                exit 1
            fi

            echo "Create Custom Domain"
            aws apigateway create-domain-name \\
                --domain-name \$customDomain \\
                --certificate-name wildcard.${req.environment}.${req.domain} \\
                --certificate-body file://${req.environment}.\${certSuffix}.1.cer \\
                --certificate-private-key file://${req.environment}.\${certSuffix}.2.key \\
                --certificate-chain file://${req.environment}.\${certSuffix}.3.cer \\
                --region ${req.region} \\
                --profile ${req.profile}
        else
            echo "Custom Domain already exists @ \$customDomain"
        fi

        aws apigateway get-domain-name --domain-name \$customDomain \\
            --region ${req.region} --profile ${req.profile} > api-domain-output.json

        cat api-domain-output.json | ${jq} '.distributionDomainName' -r > distributionDomainName
        cat api-domain-output.json | ${jq} '.domainName' -r > domainName
    """

    writeFile file: "gateway-infra.sh", text: script
    sh "chmod +x gateway-infra.sh && ./gateway-infra.sh"

    def distributionDomainName = readFile('distributionDomainName').toString().trim()
    def domainName = readFile('domainName').toString().trim()

    return [
            domainName            : domainName,
            distributionDomainName: distributionDomainName
    ]
}

return this;
