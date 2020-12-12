package tv.mediadistillery.automator.scripting;

public interface ScriptSource {

	void registerListener(ScriptSourceListener l);

	void init();
	
}
