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
    var event = { key: "test.testevent" };
    eventbus.broadcast(event);
}


var run = function() {
}

var destroy = function() {
}