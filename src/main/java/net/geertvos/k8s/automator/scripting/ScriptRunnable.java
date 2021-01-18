package net.geertvos.k8s.automator.scripting;

class ScriptRunnable implements Runnable {

	private Script script;

	public ScriptRunnable(Script script) {
		this.script = script;
	}
	
	@Override
	public void run() {
		try {
			script.execute();
		} catch(Throwable t) {
			script.onError(t);
		}
	}
	
}