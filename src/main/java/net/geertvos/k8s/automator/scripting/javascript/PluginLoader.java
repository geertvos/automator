package net.geertvos.k8s.automator.scripting.javascript;

import java.util.List;

public class PluginLoader {

	private final JavascriptScript script;
	private final List<JavascriptPluginModule> plugins;
	
	public PluginLoader(JavascriptScript script, List<JavascriptPluginModule> plugins) {
		this.script = script;
		this.plugins = plugins;
	}
	
	public void load(String name) {
		for(JavascriptPluginModule plugin : plugins) {
			if(plugin.getName().equals(name)) {
				plugin.initializePlugin(script);
			}
		}
	}
	
}

