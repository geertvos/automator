package net.geertvos.k8s.automator.scripting.plugins.logging;

import javax.script.ScriptContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class Log4jPlugin implements JavascriptPluginModule {


	@Autowired
	public Log4jPlugin()  {
	}
	
	@Override
	public void initializePlugin(JavascriptScript script) {
		try {
			Logger log = LogManager.getLogger(script.getName());
		    script.getContext().setAttribute(getName(), log, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "log";
	}

}
