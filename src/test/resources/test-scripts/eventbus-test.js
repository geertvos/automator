var onReceiveEvent = function(event) {
    test.callFunction();
}

var onReceiveTestEvent = function(event) {
    test.callTestFunction();
}

var init = function() {
    plugins.load("eventbus");
    eventbus.register(onReceiveEvent, "scripts");
    eventbus.register(onReceiveTestEvent, "test");
}


var run = function() {
    var event = { key: "test.testevent" };
    eventbus.broadcast(event);
}

var destroy = function() {
}