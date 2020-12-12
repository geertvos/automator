package net.geertvos.k8s.automator.scripting.events;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.script.Invocable;

import org.springframework.stereotype.Component;

@Component
public class AutomatorEventBus {

	private final Map<String,List<EventBusListener>> listeners = new HashMap<>();
	
	public void register(Function<Event, Void> f, String key) {
		EventBusListener listener = new EventBusListener() {
			
			@Override
			public void onReceiveEvent(Event event) {
				f.apply(event);
			}
		};
		if(listeners.containsKey(key)) {
			listeners.get(key).add(listener);
		} else {
			listeners.put(key, new LinkedList<>());
			listeners.get(key).add(listener);
		}
	}
	
	public void broadcast(Event event) {
		List<EventBusListener> receivers = listeners.get(event.getSubscriptionKey());
		if(receivers != null) {
			for(EventBusListener l : receivers) {
				l.onReceiveEvent(event);
			}
		}
	}
	
}
