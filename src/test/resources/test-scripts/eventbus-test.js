var onReceiveEvent = function(event) {
    test.callFunction();
}

var init = function() {
    plugins.load("eventbus");
    eventbus.register(onReceiveEvent, "scripts");
}

var run = function() {
}

var destroy = function() {
}