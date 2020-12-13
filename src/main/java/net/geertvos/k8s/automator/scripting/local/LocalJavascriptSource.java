package net.geertvos.k8s.automator.scripting.local;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import net.geertvos.k8s.automator.scripting.events.AutomatorEventBus;
import net.geertvos.k8s.automator.scripting.javascript.AbstractJavascriptSource;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

public class LocalJavascriptSource extends AbstractJavascriptSource {

	private static final Logger LOG = LogManager.getLogger(LocalJavascriptSource.class);
	private final String localFile;

	@Autowired
	public LocalJavascriptSource(ApplicationContext context, AutomatorEventBus eventBus, String localFile) {
		super(context, eventBus); 
		 if(localFile == null) {
			 throw new IllegalArgumentException("localFile cannot be null.");
		 }
		 this.localFile = localFile;
	}

	@Override
	public void init() {
		File newScript = new File(localFile);
		if(isJavascriptFile(newScript)) {
			loadScript("local", newScript);
		}
	}

	
	private boolean isJavascriptFile(File file) {
		return file.getName().endsWith(".js");
	}

	
	private void loadScript(String name, File file) {
		try {
			LOG.info("Loading script" + name);
			JavascriptScript script = new JavascriptScript(name, file);
			onScriptAdded(script);
		} catch (Exception e) {
			LOG.error("Unable to load script: " + file, e);
		}
	}


}
