package tv.mediadistillery.automator.scripting.plugins.rabbitmq;

import java.util.List;

import javax.script.ScriptContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;

import tv.mediadistillery.automator.scripting.javascript.JavascriptPlugin;
import tv.mediadistillery.automator.scripting.javascript.JavascriptPluginModule;
import tv.mediadistillery.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class RabbitMQPlugin implements JavascriptPluginModule {


	private Client client;

	@Autowired
	public RabbitMQPlugin()  {
		// For documentation about this plug-in see:
		// https://github.com/rabbitmq/hop
	}
	
	@Override
	public void initializePlugin(JavascriptScript script) {
		try {
			String managementURL = System.getenv("RABBITMQ_URL");
			String managementUsername = System.getenv("RABBITMQ_USERNAME");
			String managementPassword = System.getenv("RABBITMQ_PASSWORD");
			
			client = new Client(
					  new ClientParameters()
					    .url(managementURL)
					    .username(managementUsername)
					    .password(managementPassword)
					);
		    script.getContext().setAttribute("rabbitmq", client, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "rabbitmq";
	}

}
