package net.geertvos.k8s.automator.scripting.plugins.config;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;
import org.springframework.beans.factory.annotation.Autowired;

import javax.script.ScriptContext;

@JavascriptPlugin
public class ConfigurationManagerPlugin implements JavascriptPluginModule {

    private SpringCloudPropertyProvider springCloudPropertyProvider;

    @Autowired
    public ConfigurationManagerPlugin(SpringCloudPropertyProvider springCloudPropertyProvider) {
        this.springCloudPropertyProvider = springCloudPropertyProvider;
    }

    @Override
    public void initializePlugin(JavascriptScript script) {
        try {
            script.getContext().setAttribute(getName(), springCloudPropertyProvider, ScriptContext.ENGINE_SCOPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "config";
    }

}
