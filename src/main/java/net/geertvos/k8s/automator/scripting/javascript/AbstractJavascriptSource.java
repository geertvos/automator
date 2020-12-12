package net.geertvos.k8s.automator.scripting.javascript;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import net.geertvos.k8s.automator.scripting.AbstractScriptSource;

public abstract class AbstractJavascriptSource extends AbstractScriptSource {

	private static final Logger LOG = LogManager.getLogger(AbstractJavascriptSource.class);
	protected final List<JavascriptPluginModule> plugins = new LinkedList<>();

	public AbstractJavascriptSource(ApplicationContext context) {
		loadPlugins(context);
	}
	

	protected void onScriptAdded(JavascriptScript script) {
		PluginLoader loader = new PluginLoader(script, plugins);
		script.getContext().setAttribute("plugins", loader, ScriptContext.ENGINE_SCOPE);
		super.onScriptAdded(script);
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
