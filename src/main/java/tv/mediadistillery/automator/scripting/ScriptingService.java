package tv.mediadistillery.automator.scripting;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScriptingService implements ScriptSourceListener {

	private final List<Script> activeScripts = new LinkedList<>();
	private ScriptSource source; 
	private static final Logger LOG = LogManager.getLogger(ScriptingService.class);
	
	@Autowired
	public ScriptingService(ScriptSource source) {
		this.source = source;
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
	}
	

	@Override
	public void onScriptAdded(Script script) {
		try {
			script.init();
		} catch (Exception e) {
			LOG.error("Script initialization for script "+script.getName()+" failed.", e);
		}
		activeScripts.add(script);
	}

	@Override
	public void onScriptRemoved(Script script) {
		activeScripts.remove(script);
		try {
			script.destroy();
		} catch(Exception e) {
			LOG.error("Script destructor for script "+script.getName()+" failed.", e);
		}
	}
	
}
