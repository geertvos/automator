package net.geertvos.k8s.automator.scripting.plugins.slack;

import javax.script.ScriptContext;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class SlackJavascriptPlugin implements JavascriptPluginModule {

	@Override
	public void initializePlugin(JavascriptScript script) {
		try {
		    script.getContext().setAttribute(getName(), new SlackApi(), ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "slack";
	}

}
