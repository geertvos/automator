var init = function() {
    plugins.load("config");
    plugins.load("streamconfig")

}

var run = function() {
    print(config.getProperty("clustering.backend"))
}

var destroy = function() {
}
