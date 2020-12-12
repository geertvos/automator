package net.geertvos.k8s.automator.scripting;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.geertvos.k8s.automator.scripting.events.AutomatorEventBus;
import net.geertvos.k8s.automator.scripting.events.DefaultAutomatorEvent;

@Component
public class ScriptingService implements ScriptSourceListener {

	private static final Logger LOG = LogManager.getLogger(ScriptingService.class);
	private final List<Script> activeScripts = new LinkedList<>();
	private final ScriptSource source;
	private final AutomatorEventBus eventBus; 

	
	@Autowired
	public ScriptingService(ScriptSource source, AutomatorEventBus eventBus) {
		this.source = source;
		this.eventBus = eventBus;
		source.registerListener(this);
	}
	
	@PostConstruct
	public void postConstruct() {
		source.init();
	}
	
	@Scheduled(fixedRate=60000)
	public void runScripts() {
		//For now we run script periodically, later we can add more advanced features
		LOG.info("Executing "+activeScripts.size()+" scripts.");
		for(Script script : activeScripts) {
			try {
				script.execute();
			} catch(Throwable t) {
				script.onError(t);
			}
		}
		eventBus.broadcast(new DefaultAutomatorEvent("scripts.executed", "All scripts executed."));
	}
	

	@Override
	public void onScriptAdded(Script script) {
		try {
			script.init();
			activeScripts.add(script);
		} catch (Throwable e) {
			LOG.error("Script initialization for script "+script.getName()+" failed.", e);
		}
	}

	@Override
	public void onScriptRemoved(Script script) {
		activeScripts.remove(script);
		try {
			script.destroy();
		} catch(Throwable e) {
			LOG.error("Script destructor for script "+script.getName()+" failed.", e);
		}
	}
	
}
