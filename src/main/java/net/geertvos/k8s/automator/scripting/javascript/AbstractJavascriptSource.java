package net.geertvos.k8s.automator.scripting.javascript;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import net.geertvos.k8s.automator.scripting.AbstractScriptSource;
import net.geertvos.k8s.automator.scripting.Script;
import net.geertvos.k8s.automator.scripting.events.AutomatorEventBus;
import net.geertvos.k8s.automator.scripting.events.DefaultAutomatorEvent;

public abstract class AbstractJavascriptSource extends AbstractScriptSource {

	private static final Logger LOG = LogManager.getLogger(AbstractJavascriptSource.class);
	
	protected final List<JavascriptPluginModule> plugins = new LinkedList<>();
	protected final AutomatorEventBus eventBus;

	public AbstractJavascriptSource(ApplicationContext context, AutomatorEventBus eventBus) {
		loadPlugins(context);
		this.eventBus = eventBus;
	}
	

	protected void onScriptAdded(JavascriptScript script) {
		JavascriptPluginLoader loader = new JavascriptPluginLoader(script, plugins);
		script.getContext().setAttribute("plugins", loader, ScriptContext.ENGINE_SCOPE);
		super.onScriptAdded(script);
		eventBus.broadcast(new DefaultAutomatorEvent("scripts.new", "New script loaded."));
	}


	@Override
	protected void onScriptRemoved(Script script) {
		super.onScriptRemoved(script);
		eventBus.broadcast(new DefaultAutomatorEvent("scripts.removed", "Script removed "+script.getName()));
	}


	@Override
	public void init() {
	}


	private void loadPlugins(ApplicationContext context) {
		Map<String, Object> loadedPlugins = context.getBeansWithAnnotation(JavascriptPlugin.class);
		for(Object o : loadedPlugins.values()) {
			if(o instanceof JavascriptPluginModule) {
				plugins.add((JavascriptPluginModule) o);
			} else {
				LOG.error("Plugin "+o.getClass()+" is not a JavascriptPluginModule");
			}
		}
	}


}
