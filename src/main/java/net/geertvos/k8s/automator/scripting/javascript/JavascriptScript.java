package net.geertvos.k8s.automator.scripting.javascript;

import net.geertvos.k8s.automator.scripting.Script;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JavascriptScript implements Script {

	private static final Logger LOG = LogManager.getLogger(JavascriptScript.class);
	
	private final ScriptEngine engine;
	private final ScriptContext ctx;
	private final String sourceCode;
	private final String name;
	private final long lastModified;

	private static final String INIT_FUNCTION = "init";
	private static final String RUN_FUNCTION = "run";
	private static final String DESTROY_FUNCTION = "destroy";
	private static final String CRON_SCHEDULE = "cronSchedule";
	
	public JavascriptScript(String scriptName, File file) throws IOException {
		this.name = scriptName;
	    this.ctx = new SimpleScriptContext();
		NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
		this.engine = factory.getScriptEngine();
	    ctx.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
	    this.lastModified = file.lastModified();
		this.sourceCode = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	}

	public void initPlugin(JavascriptPluginModule plugin) {
			plugin.initializePlugin(this);
	}
	
	public String getName() {
		return name;
	}
	
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public void init() throws Exception {
		engine.eval(sourceCode, ctx);
		if (ctx.getAttribute(INIT_FUNCTION) != null) {
			engine.eval("init()", ctx);
		}
	}

	@Override
	public void execute() throws Exception {
		if (ctx.getAttribute(RUN_FUNCTION) != null) {
			engine.eval("run()", ctx);
		} else {
			LOG.error("Plugin '{}' does not define run() function. Not executing plugin logic.", name);
		}
	}

	@Override
	public void destroy() throws Exception {
		if (ctx.getAttribute(DESTROY_FUNCTION) != null) {
			engine.eval("destroy()", ctx);
		}
	}
	
	public ScriptEngine getEngine() {
		return this.engine;
	}
	
	public ScriptContext getContext() {
		return this.ctx;
	}

	@Override
	public void onError(Throwable t) {
		LOG.error("Unhandled exception while executing the Javascript script '{}'", name, t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean requiresSchedule() {
		Map<String,Object> settings = (Map<String, Object>) ctx.getAttribute("settings");
        return settings != null && settings.containsKey(CRON_SCHEDULE);
    }

	@SuppressWarnings("unchecked")
	@Override
	public String getCronSchedule() {
		Map<String,Object> settings = (Map<String, Object>) ctx.getAttribute("settings");
		if (settings != null && settings.containsKey(CRON_SCHEDULE)) {
			return String.valueOf(settings.get(CRON_SCHEDULE));
		}
		return null;
	}

}
