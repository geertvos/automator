package net.geertvos.k8s.automator.scripting.events;

public interface EventBusListener {

	void onReceiveEvent(Event event);
	
}
