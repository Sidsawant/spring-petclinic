#!groovy

def build(def args){
    
	
    ${tool 'maven311'} mvn clean install -Dmaven.test.skip=true  
}

return this;
