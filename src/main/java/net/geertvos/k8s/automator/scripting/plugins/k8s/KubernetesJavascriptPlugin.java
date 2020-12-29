package net.geertvos.k8s.automator.scripting.plugins.k8s;

import javax.script.ScriptContext;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class KubernetesJavascriptPlugin implements JavascriptPluginModule {

	@Override
	public void initializePlugin(JavascriptScript script) {
		try {
			KubernetesClient client = new DefaultKubernetesClient();
//			Deployment deployment = client.apps()
//										  .deployments()
//										  .withName("")
//										  .get();
//			deployment.setSpec(deployment.getSpec().setReplicas(););
//			deployment.getSpec().setReplicas();
			script.getContext().setAttribute(getName(), client, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "k8s";
	}

}
