package net.geertvos.k8s.automator.scripting.javascript;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.geertvos.k8s.automator.scripting.Script;

public class JavascriptScript implements Script {

	private static final String NASHORN_ENGINE = "nashorn";

	private static final Logger LOG = LogManager.getLogger(JavascriptPlugin.class);
	
	private final ScriptEngine engine;
	private final ScriptContext ctx;
	private final String sourceCode;
	private final String name;
	private final long lastModified;
	
	public JavascriptScript(String scriptName, File file) throws Exception {
		this.name = scriptName;
		ScriptEngineManager factory = new ScriptEngineManager();
	    this.ctx = new SimpleScriptContext();
		this.engine = factory.getEngineByName(NASHORN_ENGINE);		
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
		if(ctx.getAttribute("init") != null) {
			engine.eval("init()", ctx);
		}
	}

	@Override
	public void execute() throws Exception {
		if(ctx.getAttribute("run") != null) {
			engine.eval("run()", ctx);
		} else {
			LOG.error("Plugin '"+name+"' does not define run() function. Not executing plugin logic.");
		}
	}

	@Override
	public void destroy() throws Exception {
		if(ctx.getAttribute("destroy") != null) {
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
		LOG.error("Unhandled exception while executing the Javascript script '"+name+"'", t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean requiresSchedule() {
		Map<String,Object> settings = (Map<String, Object>) ctx.getAttribute("settings");
		if(settings != null && settings.containsKey("cronSchedule")) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getCronSchedule() throws Exception {
		Map<String,Object> settings = (Map<String, Object>) ctx.getAttribute("settings");
		if(settings != null && settings.containsKey("cronSchedule")) {
			return String.valueOf(settings.get("cronSchedule"));
		}
		return null;
	}

}
