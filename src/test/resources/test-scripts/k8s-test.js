var init = function() {
    plugins.load("k8s");
    plugins.load("log");
    log.info("Running k8s test");
}

var run = function() {
    var myServices = k8s.services().list();
    var count = myServices.getItems().size();
    log.info("Found "+count+" services");
    for(i=0;i<count;i++) {
      var service = myServices.getItems().get(i);
      log.info(service);
      print(service.getMetadata().getName());
      
    }
    log.info("Found "+count+" services");
}

var destroy = function() {
}