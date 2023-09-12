package net.geertvos.k8s.automator.scripting.plugins.rabbitmq_shovel;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

import javax.script.ScriptContext;

@JavascriptPlugin
public class RabbitMQShovelPlugin implements JavascriptPluginModule {

    @Override
    public void initializePlugin(JavascriptScript script) {
        try {
            script.getContext().setAttribute(getName(), new ShovelClient(), ScriptContext.ENGINE_SCOPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "shovel";
    }
}
