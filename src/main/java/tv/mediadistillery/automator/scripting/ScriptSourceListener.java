package tv.mediadistillery.automator.scripting;

public interface ScriptSourceListener {

	void onScriptAdded(Script script);
	
	void onScriptRemoved(Script script);
}
