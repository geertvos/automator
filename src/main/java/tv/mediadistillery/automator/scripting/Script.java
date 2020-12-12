package tv.mediadistillery.automator.scripting;

public interface Script {

	void init() throws Exception;
	
	void execute() throws Exception;
	
	void destroy() throws Exception;
	
	void onError(Throwable t);
	
	String getName();
	
}
