#!groovy

def build(def args){
    
	echo('in maven')
	 def mvnHome = tool 'maven311'
	 echo('===========starting build=============================')
				withEnv(["JAVA_HOME=${tool '1.8'}"]) {
				 bat "${mvnHome}/bin/mvn install -DskipTests"
    // some block
}
    //${tool 'maven311'} mvn clean install -Dmaven.test.skip=true  
}

return this;
