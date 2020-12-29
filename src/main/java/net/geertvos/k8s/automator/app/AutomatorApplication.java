package net.geertvos.k8s.automator.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.geertvos.k8s.automator.scripting.ScriptSource;
import net.geertvos.k8s.automator.scripting.events.AutomatorEventBus;
import net.geertvos.k8s.automator.scripting.git.GitJavascriptSource;
import net.geertvos.k8s.automator.scripting.local.LocalJavascriptSource;
import org.springframework.web.client.RestTemplate;
import tv.mediadistillery.foundation.boot2.configuration.SpringCloudConfigurationManager;
import tv.mediadistillery.foundation.configuration.BasicConfigurationManager;
import tv.mediadistillery.foundation.configuration.ConfigurationManager;

import java.util.HashMap;


@EnableScheduling
@SpringBootApplication
@Import({})
@ComponentScan(basePackages = {
		"net.geertvos.k8s.automator",
 })
public class AutomatorApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
        return builder.sources(AutomatorApplication.class);
    }


    @Bean
    public ScriptSource scriptSource(ApplicationContext context, AutomatorEventBus eventBus) {
    	String file = System.getenv("JS_FILE");
    	if(file != null) {
    		return new LocalJavascriptSource(context, eventBus, file);
    	}
    	return new GitJavascriptSource(context, eventBus);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ConfigurationManager configurationManager(Environment environment) {
        String configSource = System.getenv("CONFIG_SOURCE");
        if (configSource != null && configSource.equals("spring.cloud")) {
            return new SpringCloudConfigurationManager(environment);
        }
        return new BasicConfigurationManager(new HashMap<>(System.getenv()));
    }
    
    public static void main(final String[] args) {
    	SpringApplication application = new SpringApplication(AutomatorApplication.class);
    	application.setBanner(new AutomatorBanner());
    	application.run(args);
    }
    
    
}


