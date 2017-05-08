#!groovy

def build(def args){
    
	echo('in maven")
    //${tool 'maven311'} mvn clean install -Dmaven.test.skip=true  
}

return this;
