package net.geertvos.k8s.automator.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;


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

    public static void main(final String[] args) {
    	SpringApplication application = new SpringApplication(AutomatorApplication.class);
    	application.setBanner(new AutomatorBanner());
    	application.run(args);
    }
    
    
}


