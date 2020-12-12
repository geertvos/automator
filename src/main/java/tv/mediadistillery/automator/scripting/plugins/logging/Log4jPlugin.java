package tv.mediadistillery.automator.scripting.plugins.logging;

import javax.script.ScriptContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import tv.mediadistillery.automator.scripting.javascript.JavascriptPlugin;
import tv.mediadistillery.automator.scripting.javascript.JavascriptPluginModule;
import tv.mediadistillery.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class Log4jPlugin implements JavascriptPluginModule {


	@Autowired
	public Log4jPlugin()  {
	}
	
	@Override
	public void initializePlugin(JavascriptScript script) {
		try {
			Logger log = LogManager.getLogger(script.getName());
		    script.getContext().setAttribute("log", log, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "log";
	}

}
