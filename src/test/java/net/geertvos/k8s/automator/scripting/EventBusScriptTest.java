package net.geertvos.k8s.automator.scripting;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import net.geertvos.k8s.automator.scripting.events.AutomatorEventBus;
import net.geertvos.k8s.automator.scripting.events.DefaultAutomatorEvent;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginLoader;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;
import net.geertvos.k8s.automator.scripting.plugins.eventbus.EventBusPlugin;
import net.geertvos.k8s.automator.scripting.plugins.logging.Log4jPlugin;

@RunWith(SpringRunner.class)
public class EventBusScriptTest {

	private List<JavascriptPluginModule> plugins = new LinkedList<>();
	private AutomatorEventBus eventBus = new AutomatorEventBus();
	private boolean functionCalled = false;
	private boolean testFunctionCalled = false;
	private JavascriptScript script;
	
	@Before()
	public void setup() throws Exception {
		plugins.add(new Log4jPlugin());
		plugins.add(new EventBusPlugin(eventBus));
		script = new JavascriptScript("test", new File("./src/test/resources/test-scripts/eventbus-test.js"));
		JavascriptPluginLoader loader = new JavascriptPluginLoader(script, plugins);
		script.getContext().setAttribute("plugins", loader, ScriptContext.ENGINE_SCOPE);
		script.getContext().setAttribute("test", this, ScriptContext.ENGINE_SCOPE);
		script.init();
	}
	
	
	@Test()
	public void testScript() throws Exception {
		script.execute();
		eventBus.broadcast(new DefaultAutomatorEvent("scripts.test", "New test event fired."));
		Assert.assertTrue("Callback should have been called by the engine.", functionCalled);
		Assert.assertTrue("Test callback from JS event should have been called by the engine.", testFunctionCalled);
	}
	
	@After
	public void destroy() throws Exception {
		script.destroy();
	}
	
	public void callFunction() {
		functionCalled = true;
	}
	
	
	public void callTestFunction() {
		testFunctionCalled = true;
	}
	
	
	
}
