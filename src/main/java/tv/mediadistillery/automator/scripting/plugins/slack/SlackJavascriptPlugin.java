package tv.mediadistillery.automator.scripting.plugins.slack;

import javax.script.ScriptContext;

import tv.mediadistillery.automator.scripting.javascript.JavascriptPlugin;
import tv.mediadistillery.automator.scripting.javascript.JavascriptPluginModule;
import tv.mediadistillery.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class SlackJavascriptPlugin implements JavascriptPluginModule {

	@Override
	public void initializePlugin(JavascriptScript script) {
		try {
		    script.getContext().setAttribute("slack", new SlackApi(), ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "slack";
	}

}
