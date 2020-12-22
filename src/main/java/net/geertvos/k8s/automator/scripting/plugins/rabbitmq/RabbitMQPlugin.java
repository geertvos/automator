package net.geertvos.k8s.automator.scripting.plugins.rabbitmq;

import javax.script.ScriptContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class RabbitMQPlugin implements JavascriptPluginModule {

	private static final Logger LOG = LogManager.getLogger(RabbitMQPlugin.class);

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
			if(managementURL == null) {
				LOG.error("RabbitMQ client is not setup correctly and will not be available in scripts. Please provide environment variables RABBITMQ_URL, RABBITMQ_USERNAME and RABBITMQ_PASSWORD");
				return;
			}
			client = new Client(
					  new ClientParameters()
					    .url(managementURL)
					    .username(managementUsername)
					    .password(managementPassword)
					);
		    script.getContext().setAttribute(getName(), client, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "rabbitmq";
	}

}
