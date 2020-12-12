package net.geertvos.k8s.automator.scripting.events;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class AutomatorEventBus {

	private final Logger LOG = LogManager.getLogger(AutomatorEventBus.class);
	private final Map<String, List<EventBusListener>> listeners = new HashMap<>();
	
	/**
	 * Register an event handler function under the specified key. 
	 * MAtching events with handlers is done based on event.startsWith(key)
	 * @param f Callback
	 * @param key The event key to match
	 */
	public void register(Function<Event, Void> f, String key) {
		EventBusListener listener = new EventBusListener() {
			
			@Override
			public void onReceiveEvent(Event event) {
				f.apply(event);
			}
		};
		if(!listeners.containsKey(key)) {
			listeners.put(key, new LinkedList<>());
		}
		listeners.get(key).add(listener);
	}
	
	/**
	 * Broad an event to the bus to all handlers that registered for the correct keys.
	 * @param event
	 */
	public void broadcast(Event event) {
		for(String key : listeners.keySet()) {
			if(event.getSubscriptionKey().startsWith(key)) {
				List<EventBusListener> keyKisteners = listeners.get(key);
				for(EventBusListener listener : keyKisteners) {
					try {
						listener.onReceiveEvent(event);
					} catch(Exception e) {
						LOG.error("Unable to execute handler for event "+event.getSubscriptionKey(), e);
					}
				}
			}
		}
	}
	
}
