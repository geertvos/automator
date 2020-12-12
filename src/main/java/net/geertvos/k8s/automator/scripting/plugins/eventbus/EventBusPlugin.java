package net.geertvos.k8s.automator.scripting.plugins.eventbus;

import javax.script.ScriptContext;

import org.springframework.beans.factory.annotation.Autowired;

import net.geertvos.k8s.automator.scripting.events.AutomatorEventBus;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class EventBusPlugin implements JavascriptPluginModule {

	private final AutomatorEventBus eventBus;

	@Autowired
	public EventBusPlugin(AutomatorEventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	@Override
	public void initializePlugin(JavascriptScript script) {
		try {
		    script.getContext().setAttribute("eventbus", eventBus, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "eventbus";
	}

}
