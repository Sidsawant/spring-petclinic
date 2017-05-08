#!groovy

def example() {

    s3.publish([
            bucket : 'some bucket', region: 'us-east-1', account: 'dev',
            entries: [
                    [flatten: false, file: "infra/"],
                    [flatten: false, file: "gateway/"]
            ]
    ])

}

def publish(def req) {
    def entries = []

    for (entry in req.entries) {
        entries << [
                bucket                 : req.bucket,
                excludedFile           : '',
                flatten                : entry.flatten,
                gzipFiles              : false,
                keepForever            : false,
                managedArtifacts       : true,
                noUploadOnFailure      : true,
                selectedRegion         : req.region,
                sourceFile             : entry.file,
                storageClass           : 'STANDARD',
                uploadFromSlave        : true,
                useServerSideEncryption: false
        ]
    }

    step([
            $class                              : 'S3BucketPublisher',
            dontWaitForConcurrentBuildCompletion: false,
            entries                             : entries,
            profileName                         : "ArtifactPublisher${req.account.capitalize()}",
            userMetadata                        : []
    ])
}

return this;