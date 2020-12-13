package net.geertvos.k8s.automator.scripting.plugins.k8s;

import java.io.FileReader;
import java.io.IOException;

import javax.script.ScriptContext;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPlugin;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptPluginModule;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

@JavascriptPlugin
public class KubernetesJavascriptPlugin implements JavascriptPluginModule {

	@Override
	public void initializePlugin(JavascriptScript script) {
		try {
			CoreV1Api api = null;
			ApiClient client = null;
			String kubeConfigPath = System.getenv("KUBE_CONFIG");
			if(kubeConfigPath == null) {
				client = ClientBuilder.cluster().build();
			} else {
				client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
			}
			Configuration.setDefaultApiClient(client);
			api = new CoreV1Api();
		    script.getContext().setAttribute(getName(), api, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "k8s";
	}

}
