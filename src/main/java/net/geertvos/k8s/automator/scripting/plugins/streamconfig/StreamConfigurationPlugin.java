package net.geertvos.k8s.automator.scripting.plugins.streamconfig;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;
import org.springframework.beans.factory.annotation.Autowired;
import tv.mediadistillery.foundation.stream.StreamConfigurationManager;

import javax.script.ScriptContext;

@JavascriptPlugin
public class StreamConfigurationPlugin implements JavascriptPluginModule {

    private final StreamConfigurationManager streamConfigurationManager;

    @Autowired
    public StreamConfigurationPlugin(StreamConfigurationManager streamConfigurationManager) {
        this.streamConfigurationManager = streamConfigurationManager;
    }

    @Override
    public void initializePlugin(JavascriptScript script) {
        try {
            script.getContext().setAttribute(getName(), streamConfigurationManager, ScriptContext.ENGINE_SCOPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "streamconfig";
    }

}
