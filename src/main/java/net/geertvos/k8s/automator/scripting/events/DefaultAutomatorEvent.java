package net.geertvos.k8s.automator.scripting.events;

import java.util.HashMap;
import java.util.Map;

public class DefaultAutomatorEvent implements Event {

	private final String key;
	private final String message;
	private final Map<String, Object> properties = new HashMap<>();
	
	public DefaultAutomatorEvent(String subscriptionKey, String message) {
		this.key = subscriptionKey;
		this.message = message;
	}
	
	@Override
	public String getSubscriptionKey() {
		return key;
	}
	
	public String getMessage() {
		return message;
	}

	public void set(String key, Object value) {
		this.properties.put(key, value);
	}
	
	public Object get(String key) {
		return this.properties.get(key);
	}
}
