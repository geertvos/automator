package net.geertvos.k8s.automator.scripting;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractScriptSource implements ScriptSource {

	private final List<ScriptSourceListener> listeners = new LinkedList<>();
	
	@Override
	public void registerListener(ScriptSourceListener l) {
		this.listeners.add(l);
	}

	protected void onScriptAdded(Script script) {
		for(ScriptSourceListener l : listeners) {
			l.onScriptAdded(script);
		}
	}

	protected void onScriptRemoved(Script script) {
		for(ScriptSourceListener l : listeners) {
			l.onScriptRemoved(script);
		}
	}

	@Override
	public abstract void init();

}
