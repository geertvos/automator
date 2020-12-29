var init = function() {
    plugins.load("config");
    plugins.load("streamconfig");
    plugins.load("k8s");
    plugins.load("log");
}

var run = function() {
    deploymentNames = ["md-overlay-detector",
                       "md-programeventdetection-service",
                       "md-indexer-service"]
    enabledStreams = streamconfig.getStreams().size();
    log.info("desired capacity for all deployments is: " + enabledStreams);
    deploymentNames.forEach(function(deploymentName) {
        deployment = k8s.apps().deployments().withName(deploymentName).get();
        maxResources = config.getProperty(deploymentName, "clusteredQueue.maxResources");
        desiredReplicas = Math.ceil(enabledStreams / maxResources) + 1
        if (desiredReplicas != deployment.getSpec().getReplicas()) {
            log.info("Setting desired replicas of " + deploymentName + " to " + desiredReplicas)
            k8s.apps().deployments().inNamespace("default").withName(deploymentName).scale(desiredReplicas)
        }
    });
}

var destroy = function() {
}
