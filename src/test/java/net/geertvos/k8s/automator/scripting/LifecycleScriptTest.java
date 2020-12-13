package net.geertvos.k8s.automator.scripting;

import java.io.File;

import javax.script.ScriptContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

@RunWith(SpringRunner.class)
public class LifecycleScriptTest {

	private JavascriptScript script;
	private boolean initCalled;
	private boolean runCalled;
	private boolean destroyCalled;
	
	@Before()
	public void setup() throws Exception {
		script = new JavascriptScript("test", new File("./src/test/resources/test-scripts/lifecycle-test.js"));
		script.getContext().setAttribute("test", this, ScriptContext.ENGINE_SCOPE);
		script.init();
		Assert.assertTrue("Init function must have been called by the engine.", initCalled);
	}
	
	
	@Test()
	public void testScript() throws Exception {
		script.execute();
		Assert.assertTrue("Run function must have been called by the engine.", runCalled);
	}
	
	@After
	public void destroy() throws Exception {
		script.destroy();
		Assert.assertTrue("Destroy function must have been called by the engine.", destroyCalled);
	}
	
	public void initCalled() {
		initCalled = true;
	}
	
	public void runCalled() {
		runCalled = true;
	}
	
	public void destroyCalled() {
		destroyCalled = true;
	}
	
	
	
}
