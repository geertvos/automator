package net.geertvos.k8s.automator.scripting.plugins.streamconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import tv.mediadistillery.foundation.api.client.RemoteStreamConfigurationManager;
import tv.mediadistillery.foundation.api.client.internal.HttpStreamConfigurationServiceClientFactory;
import tv.mediadistillery.foundation.api.client.internal.StreamConfigurationClientFactory;
import tv.mediadistillery.foundation.boot2.configuration.SpringCloudConfigurationManager;
import tv.mediadistillery.foundation.configuration.ConfigurationManager;
import tv.mediadistillery.foundation.stream.StreamConfigurationManager;

@Configuration
public class StreamConfigBean {

    @Bean
    public ConfigurationManager configurationManager(Environment environment) {
        return new SpringCloudConfigurationManager(environment);
    }

    @Bean
    public StreamConfigurationManager streamConfigurationManager(ConfigurationManager configurationManager,
                                                                 StreamConfigurationClientFactory streamConfigurationClientFactory) {
        return new RemoteStreamConfigurationManager(configurationManager, streamConfigurationClientFactory);
    }

    @Bean
    public StreamConfigurationClientFactory streamConfigurationClientFactory(ConfigurationManager configurationManager) {
        return new HttpStreamConfigurationServiceClientFactory(configurationManager);
    }

}
