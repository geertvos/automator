package net.geertvos.k8s.automator.scripting;

public interface Script {

	void init() throws Exception;
	
	void execute() throws Exception;
	
	void destroy() throws Exception;
	
	void onError(Throwable t);
	
	String getName();
	
	boolean requiresSchedule();
	
	String getCronSchedule() throws Exception;
}
