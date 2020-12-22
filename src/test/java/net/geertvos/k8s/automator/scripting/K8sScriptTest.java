package net.geertvos.k8s.automator.scripting;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginLoader;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;
import net.geertvos.k8s.automator.scripting.plugins.k8s.KubernetesJavascriptPlugin;
import net.geertvos.k8s.automator.scripting.plugins.logging.Log4jPlugin;

@RunWith(SpringRunner.class)
public class K8sScriptTest {

	private List<JavascriptPluginModule> plugins = new LinkedList<>();
	private JavascriptScript script;
	
	@Before()
	public void setup() throws Exception {
		plugins.add(new Log4jPlugin());
		plugins.add(new KubernetesJavascriptPlugin());
		script = new JavascriptScript("test", new File("./src/test/resources/test-scripts/k8s-test.js"));
		JavascriptPluginLoader loader = new JavascriptPluginLoader(script, plugins);
		script.getContext().setAttribute("plugins", loader, ScriptContext.ENGINE_SCOPE);
		script.getContext().setAttribute("test", this, ScriptContext.ENGINE_SCOPE);
		script.init();
	}
	
	
	@Test()
	public void testScript() throws Exception {
		script.execute();
	}
	
	@After
	public void destroy() throws Exception {
		script.destroy();
	}
	
	
	
	
}
