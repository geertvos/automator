package net.geertvos.k8s.automator.scripting.javascript;

//Crazy name... plugin is in use for the annotation
public interface JavascriptPluginModule {

	/**
	 * Initialize the plug-in for the provided Script.
	 */
	void initializePlugin(JavascriptScript script);

	/**
	 * Returns the name under which this plug-in is registered.
	 * @return
	 */
	String getName();
	
}
