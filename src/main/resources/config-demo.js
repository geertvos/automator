var init = function() {
    plugins.load("config")
    print("First Automator Demo: ")
}

var run = function() {
    deployments = k8s.apps().deployments();

}

var destroy = function() {
}
