package net.geertvos.k8s.automator.scripting.plugins.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class SpringCloudPropertyProvider {

    private static final Logger LOG = Logger.getLogger(ConfigurationManagerPlugin.class);

    private final RestTemplate restTemplate;

//    @Value("${spring.cloud.config.uri}") // TODO: use @value
    private String configServerHost;

//    @Value("${spring.cloud.config.label}")
    private String configServerLabel;

    private Map<String, PropertyResolver> propertySourceMap;

    @Autowired
    public SpringCloudPropertyProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.propertySourceMap = new HashMap<>();
        this.configServerLabel = "default";
        this.configServerHost = "http://md-spring-cloud-config";
    }

    public String getProperty(String service, String property) {
        if (!propertySourceMap.containsKey(service)) {
            refreshConfiguration(service);
            if (!propertySourceMap.containsKey(service)) {
                LOG.error(String.format("Could not refresh configuration for service %s", service));
            }
        }
        return propertySourceMap.get(service).getProperty(property);
    }

    @Scheduled(fixedRate = 60000, initialDelay = 300000)
    private void refresh() {
        propertySourceMap.forEach((key, value) -> refreshConfiguration(key));
    }

    private synchronized void refreshConfiguration(String service) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        String[] args = new String[] {service, configServerLabel};
        String uri = String.format("%s/%s/%s", configServerHost, service, configServerLabel);
        ResponseEntity<Environment> response = restTemplate.exchange(uri,
                                                                     HttpMethod.GET, httpEntity,
                                                                     Environment.class, args);
        if (response.getStatusCode() == HttpStatus.OK) {
            Environment responseBody = response.getBody();
            MutablePropertySources mutablePropertySources = new MutablePropertySources();
            responseBody.getPropertySources().forEach(propertySource -> mutablePropertySources.addLast(new MapPropertySource(propertySource.getName(),
                                                                                                                         (Map<String, Object>) propertySource.getSource())));
            propertySourceMap.put(service, new PropertySourcesPropertyResolver(mutablePropertySources));
        }
    }


}
