#!groovy

def build(def args)
    sh """
    ${tool 'Maven 3.1.0'} clean install -Dmaven.test.skip=true  -Daws.account=${args.awsAccount} -Daws.image.version=${args.version} -Daws.region=${args.awsRegion} -P aws-deploy -U -e
    """
}

return this;
