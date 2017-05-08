#!groovy

def build(def args){
    
	
    ${tool 'maven311'} clean install -Dmaven.test.skip=true  -Daws.account=1111 -Daws.image.version=222 -Daws.region=3333 -P aws-deploy -U -e
    
}

return this;
