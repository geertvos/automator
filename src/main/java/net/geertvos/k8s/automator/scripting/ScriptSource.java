package net.geertvos.k8s.automator.scripting;

public interface ScriptSource {

	void registerListener(ScriptSourceListener l);

	void init();
	
}
