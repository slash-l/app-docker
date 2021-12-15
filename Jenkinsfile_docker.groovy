node {
    def server = Artifactory.server 'demo-server'
    def rtDocker = Artifactory.docker server: server
    def buildInfo = Artifactory.newBuildInfo()
    def DOCKER_REPORITORY = 'slash-guide-docker-dev-local'
    def ARTIFACTORY_DOCKER_REGISTRY='182.92.214.141:8082/' + DOCKER_REPORITORY

    stage ('Clone') {
        git url: 'https://gitee.com/mumu79/app-docker.git'
    }

    stage ('Add properties') {
        // Attach custom properties to the published artifacts:
        rtDocker.addProperty("project-name", "app-docker").addProperty("status", "stable")
    }

//    stage ('Docker login') {
//        sh 'docker login -u liujy -p Helloljy 182.92.214.141:8082'
//    }

    stage ('Build docker image') {
        docker.build(ARTIFACTORY_DOCKER_REGISTRY + '/hello-world:${BUILD_ID}')
    }

    stage ('Push image to Artifactory') {
        buildInfo = rtDocker.push ARTIFACTORY_DOCKER_REGISTRY + '/hello-world:' + env.BUILD_ID, DOCKER_REPORITORY
    }

    stage('xray scan'){
        server.publishBuildInfo buildInfo
        def scanConfig = [
                'buildName': buildInfo.name, //构建名称
                'buildNumber': buildInfo.number //构建号
//                'failBuild': true
        ]
        def scanResult = server.xrayScan scanConfig
        echo "scanResult:" + scanResult;
    }

//    stage ('Pull image from Artifactory') {
//        sh 'docker pull 182.92.214.141:8082/slash-guide-docker-virtual/busybox'
//    }

    stage ('Publish build info') {
        server.publishBuildInfo buildInfo
    }
}
