package net.geertvos.k8s.automator.scripting;

import net.geertvos.k8s.automator.scripting.events.AutomatorEventBus;
import net.geertvos.k8s.automator.scripting.local.LocalJavascriptSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
public class LocalJsTest {

    private final AutomatorEventBus eventBus = new AutomatorEventBus();
    @Autowired
    private ApplicationContext context;

    @Test()
    public void testWhitespaceInFileName() {
        String script_path = "./src/test/resources/test-scripts/eventbus-test.js ";
        LocalJavascriptSource source = new LocalJavascriptSource(context, eventBus, script_path);
        try {
            source.init();
            assertTrue(true);
        } catch (Exception e) {
            fail("init() method threw an exception: " + e.getMessage());
        }
    }

    @Test()
    public void testWithWrongFileName() {
        String script_path = "./src/test/resources/test-scripts/eventbus-test.py";
        LocalJavascriptSource source = new LocalJavascriptSource(context, eventBus, script_path);
        try {
            source.init();
            fail("init() method should have thrown an exception.");
        } catch (Exception e) {
            assertEquals("File ./src/test/resources/test-scripts/eventbus-test.py is not a javascript file.", e.getMessage());
        }
    }

    @Test()
    public void testNonExistingFile() {
        String script_path = "./src/test/resources/test-scripts/nonexisting.js";
        LocalJavascriptSource source = new LocalJavascriptSource(context, eventBus, script_path);
        try {
            source.init();
            fail("init() method should have thrown an exception.");
        } catch (Exception e) {
            assertEquals("Unable to load script: ./src/test/resources/test-scripts/nonexisting.js", e.getMessage());
        }
    }

}

