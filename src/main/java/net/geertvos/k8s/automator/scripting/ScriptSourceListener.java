package net.geertvos.k8s.automator.scripting;

public interface ScriptSourceListener {

	void onScriptAdded(Script script);
	
	void onScriptRemoved(Script script);
}
