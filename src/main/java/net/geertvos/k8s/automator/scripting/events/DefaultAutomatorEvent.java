package net.geertvos.k8s.automator.scripting.events;

public class DefaultAutomatorEvent implements Event {

	private final String key;
	private final String message;
	
	//TODO: Add support to pass in event parameters. Best option: Map<String,Object> to match with JS
	
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

}
