package net.geertvos.k8s.automator.scripting.plugins.rest;

import java.net.URI;

import org.springframework.web.client.RestTemplate;

import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

public class RestTemplateWrapper {

	private RestTemplate restTemplate;
	private JavascriptScript script;

	public RestTemplateWrapper(RestTemplate restTemplate, JavascriptScript script) {
		this.restTemplate = restTemplate;
		this.script = script;
	}
	
	public Object getForObject(String uri) throws Exception {
		String json = restTemplate.getForObject(new URI(uri), String.class);
		return script.getEngine().eval("JSON.parse(\""+json.replace("\"", "\\\"")+"\")");
	}
	
	public Object postForObject(String uri, Object request) throws Exception {
		String json = restTemplate.postForObject(new URI(uri), request, String.class);
		return script.getEngine().eval("JSON.parse(\""+json.replace("\"", "\\\"")+"\")");
	}
	
	public Object postForObject(String uri) throws Exception {
		String json = restTemplate.postForObject(new URI(uri), null, String.class);
		return script.getEngine().eval("JSON.parse(\""+json.replace("\"", "\\\"")+"\")");
	}
	
	public void put(String uri, Object request) throws Exception {
		restTemplate.put(new URI(uri), request);
	}

	public void delete(String uri) throws Exception {
		restTemplate.delete(new URI(uri));
	}

	
}
