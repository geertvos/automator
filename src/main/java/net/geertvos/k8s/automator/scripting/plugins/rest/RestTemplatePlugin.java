package net.geertvos.k8s.automator.scripting.plugins.rest;

import javax.script.ScriptContext;

import org.springframework.web.client.RestTemplate;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class RestTemplatePlugin implements JavascriptPluginModule {

	@Override
	public void initializePlugin(JavascriptScript script) {
		RestTemplate restTemplate = new RestTemplate();
		RestTemplateWrapper wrapper = new RestTemplateWrapper(restTemplate, script);
		try {
		    script.getContext().setAttribute(getName(), wrapper, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "rest";
	}

}
