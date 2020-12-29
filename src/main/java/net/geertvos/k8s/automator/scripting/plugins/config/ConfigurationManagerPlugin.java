package net.geertvos.k8s.automator.scripting.plugins.config;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.apache.log4j.Logger;
import tv.mediadistillery.foundation.boot2.configuration.SpringCloudConfigurationManager;
import tv.mediadistillery.foundation.configuration.ConfigurationManager;

import javax.script.ScriptContext;

@JavascriptPlugin
public class ConfigurationManagerPlugin implements JavascriptPluginModule {

    private static final Logger LOG = Logger.getLogger(ConfigurationManagerPlugin.class);

    private ConfigurationManager configurationManager;

    @Autowired
    public ConfigurationManagerPlugin(Environment environment) {
        int x = 2;
        this.configurationManager = new SpringCloudConfigurationManager(environment);
    }

    @Override
    public void initializePlugin(JavascriptScript script) {
        try {
            script.getContext().setAttribute(getName(), configurationManager, ScriptContext.ENGINE_SCOPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "config";
    }

}
