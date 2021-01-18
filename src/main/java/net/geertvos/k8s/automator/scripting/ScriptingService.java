package net.geertvos.k8s.automator.scripting;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import net.geertvos.k8s.automator.scripting.events.AutomatorEventBus;
import net.geertvos.k8s.automator.scripting.events.DefaultAutomatorEvent;

@Component
public class ScriptingService implements ScriptSourceListener {

	private static final Logger LOG = LogManager.getLogger(ScriptingService.class);
	private final List<Script> activeScripts = new LinkedList<>();
	private final ScriptSource source;
	private final AutomatorEventBus eventBus; 
	private final Map<Script,ScheduledTask> taskHandles = new HashMap<>();
	private final ScheduledTaskRegistrar registrar;
	
	@Autowired
	public ScriptingService(ScriptSource source, AutomatorEventBus eventBus, TaskScheduler scheduler) {
		this.source = source;
		this.eventBus = eventBus;
		this.registrar = new ScheduledTaskRegistrar();
		registrar.setTaskScheduler(scheduler);
		source.registerListener(this);
	}
	
	@PostConstruct
	public void postConstruct() {
		source.init();
	}
	
	@Scheduled(fixedRate=60000)
	public void runScripts() {
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
			if(script.requiresSchedule()) {
				String cron = script.getCronSchedule();
				CronTask task = new CronTask(new ScriptRunnable(script), script.getCronSchedule());
				ScheduledTask scheduledTask = registrar.scheduleCronTask(task);
				taskHandles.put(script, scheduledTask);
				LOG.info("Scheduling script {} to run with cron '{}'", script.getName(), cron);
			} else {
				activeScripts.add(script);
			}
		} catch (Throwable e) {
			LOG.error("Script initialization for script "+script.getName()+" failed.", e);
		}
	}

	@Override
	public void onScriptRemoved(Script script) {
		if(script.requiresSchedule()) {
			ScheduledTask task = taskHandles.get(script);
			task.cancel();
		} else {
			activeScripts.remove(script);
		}
		try {
			script.destroy();
		} catch(Throwable e) {
			LOG.error("Script destructor for script "+script.getName()+" failed.", e);
		}
	}
	
}
