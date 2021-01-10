# Automator
Kubernetes Scripting Engine

Automator is a full fledged scripting engine for your K8S cluster to automate small tasks. Similar to what bash scripts can do for a linux box, Automator helps you on k8s. 

The basic idea is that you can quickly script APIs together to get functionality that is normally not availble. For example, one might want to create an autoscaler based on the number of queues or messages waiting in RabbitMQ, or automatically register new pods to your service based on some annotations. 

Automator will provide default plugins for Slack, K8S, Log and the internal event bus. It is extremely easy to create your own plugin and add a new API to the Automator scripting context. The syntax for the scripts is Ecmascript (Javascript).

# Scripts
Each automator script should have the following structure:
```
var init = function() {
    // Initialization will be called by automator once before execution.
}

var run = function() {
  // Automator will periodically call this run function. Existing context will be reused.
}

var destroy = function() {
  // When the script is unloaded, this destructor is called once.
}
```
A demo script can be found here: https://github.com/geertvos/automator-scripts-demo/blob/master/k8s-scaling-demo.js

# Configuring scripts #
By default Automator monitors a GIT repository and all JS files from the root directory are loaded as scripts. Configre the GIT repo to load using environment variables:
```
GIT_REPO=https://github.com/geertvos/automator-scripts-demo.git
```
If you wish to use private repositories, provide an SSH keyfile file for Automator to login on your repository:
```
GIT_KEY=/home/me/.ssh/private_key
```
There is also the option to just run a single JS file locally, set the JS_FILE environment variable and point it to a file to load. Only ".js" files.
```
JS_FILE=/home/user/myscript.js
```

# APIs #
A lot of default API implemenations are provided and can be used in the scripting language. The plugin loader can be used to load any plugin.

## Logging ##
```
var init = function() {
    plugins.load("log");
}
```

The example above loads the default logging plugin that provides access to the internal logging system. The log engine behind is log4j2.
```
log.info("This is info");
log.warn("This is a warning");
log.error("This is an error");
```

## Event Bus ##
Automator has an internal event bus that scripts can subscribe to for async callbacks. All events are broadcasted to all listeners based on the key they registered on. Event handlers can register on a root key, like "scripts" to receive all events under that key, like "scripts.loaded".
```
var onReceiveEvent = function(event) {
    log.info("Demo received event: "+event.getMessage());
}

var init = function() {
    plugins.load("log");
    plugins.load("eventbus");
    log.info("Loaded event demo.");
    eventbus.register(onReceiveEvent, "scripts.executed");
}
```

Sending events from the scripting context is also possible:
```
var event = { key: "test.testevent", myKey: "myString"};
eventbus.broadcast(event);
```
Automator will convert it into an internal event object and set the key. All other values are copied and are available as properties on the event object.

## Slack ##
The Slack API must be configured using the following ENV variables:
```
SLACK_TOKEN=xoxb-youtoken-here
```
Then a simple send metohd is available to send messages to any slack channel the plugin has access too (depening on token).
```
var init = function() {
    plugins.load("slack");
    slack.send("#dev-monitoring","Hi this is Automator! From now on I am running all your Automator scripts to help automate your k8s cluster.");
}
```

### K8S API ###
The Fabric8 Java Kubernetes API client is available as a plugin for you scripts. For documentation of the API, please check: https://github.com/fabric8io/kubernetes-client
  
The plugin will use the service credentials of the pod to get access to the API. Make sure it has the correct permissions. When you run automator locally, it will rely on your kube config file.

An example of using the plugin:
```
 list = k8s.services().list();
 for (i=0; i< list.getItems().size(); i++) {
    var service = list.getItems().get(i);
    log.info(item.getMetadata().getName());
 }
```

## RabbitMQ Management API ##
Automator also ships with a RabbitMQ management API plugin. 
Use the following environment variables to configure access:
```
RABBITMQ_PASSWORD=<your passwd>
RABBITMQ_URL=http://examplehost:15672/api/
RABBITMQ_USERNAME=<your user>
```
The RabbitMQ plugin will inject the management client, documentation here: https://github.com/rabbitmq/hop
```
nodes = rabbitmq.getNodes();
for(i=0; i < nodes.size(); i++) {
    var node = nodes.get(i);
    log.info("RabbitMQ Node: "+node.getName()+" memory used: "+node.getMemoryUsed());
}
```

## Adding your custom API ##
Adding new API plugins is extremely simple. Just mark your class with the correct annotation and inject your API object into the script. See example below. The plugin's name will be available in the scripting language. Make sure that you add your classpath to the Spring boot scanner by modifying this property in application.properties:
```
scan.packages=net.geertvos.k8s.automator, com.myapi.plugin
```

An example plugin:
```
@JavascriptPlugin
public class MyPlugin implements JavascriptPluginModule {

    @Override
    public void initializePlugin(JavascriptScript script) {
        try {
             MyApi api = new MyApi();
            script.getContext().setAttribute(getName(), api, ScriptContext.ENGINE_SCOPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "myapi";
    }

}

```
