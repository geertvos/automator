package net.geertvos.k8s.automator.scripting.git;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.geertvos.k8s.automator.scripting.events.AutomatorEventBus;
import net.geertvos.k8s.automator.scripting.javascript.AbstractJavascriptSource;
import net.geertvos.k8s.automator.scripting.javascript.JavascriptScript;

public class GitJavascriptSource extends AbstractJavascriptSource {

	private static final Logger LOG = LogManager.getLogger(GitJavascriptSource.class);
	private final String remoteUrl;
	private final Map<String, JavascriptScript> scripts = new HashMap<>();
	private File localPath;
	private Git git;
	private TransportConfigCallback transportConfigCallback;

	@Autowired
	public GitJavascriptSource(ApplicationContext context, AutomatorEventBus eventBus) {
		super(context, eventBus); 
		remoteUrl = System.getenv("GIT_REPO");
		 if(remoteUrl == null) {
			 throw new IllegalArgumentException("GIT_REPO environment variable is not set.");
		 }
	}

	@Override
	public void init() {
		initGitTransport();
		cloneRepo();
		checkFiles();
	}

	private void initGitTransport() {
		if(sshRequired()) {
			JschConfigSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
				
				@Override
				protected void configure(Host hc, Session session) {
					super.configure(hc, session);
				}
	
				@Override
				protected void configureJSch(JSch jsch) {
					super.configureJSch(jsch);
					if(hasSpecificIdentity()) {
						try {
							jsch.addIdentity(getSpecificIdentity());
						} catch (JSchException e) {
							throw new IllegalArgumentException("SSH identity file cannot be added.", e);
						}
					}
				}
			};
			transportConfigCallback = new TransportConfigCallback() {
				
				@Override
				public void configure(Transport transport) {
					SshTransport sshTransport = ( SshTransport )transport;
				    sshTransport.setSshSessionFactory( sshSessionFactory );
					
				}
			};		
		}
	}

	@Scheduled(fixedRate = 300000)
	public void pullRepo() {
		PullCommand pullCmd = git.pull();
		try {
		    pullCmd.call();
		    checkFiles();
		} catch (GitAPIException e) {
			LOG.info("Pull of repo "+remoteUrl+" failed.");
		}
	}
	
	private void cloneRepo() {
		try {
			localPath = File.createTempFile("AutomatorGitRepository", "");
	        if(!localPath.delete()) {
	        }
			LOG.info("Cloning from " + remoteUrl + " to " + localPath);
			git = Git.cloneRepository()
	                .setURI(remoteUrl)
	                .setDirectory(localPath)
	                .setTransportConfigCallback(transportConfigCallback)
	                .call();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void checkFiles() {
		Stream.of(localPath.listFiles())
			.filter(file -> !file.isDirectory())
			.filter(file -> isJavascriptFile(file))
			.forEach(file -> checkFile(file.getName()));
	}
	
	private void checkFile(String filename) {
		String fullPath = localPath.getAbsolutePath() + File.separator + filename;
		File newScript = new File(fullPath);
		if(scripts.containsKey(filename)) {
			JavascriptScript existing = scripts.get(filename);
			if(existing.getLastModified() < newScript.lastModified()) {
				LOG.info("Replacing script "+filename+" with updated version.");
				unloadScript(existing);
				loadScript(filename, newScript);
			}
		} else {
			loadScript(filename, newScript);
		}
		
	}
	
	private boolean isJavascriptFile(File file) {
		return file.getName().endsWith(".js");
	}

	private void unloadScript(JavascriptScript script) {
		JavascriptScript removed = this.scripts.remove(script.getName());
		if(removed != null) {
			onScriptRemoved(removed);
		}
	}
	
	private void loadScript(String name, File file) {
		try {
			LOG.info("Loading script" + name);
			JavascriptScript script = new JavascriptScript(name, file);
			scripts.put(name, script);
			onScriptAdded(script);
		} catch (Exception e) {
			LOG.error("Unable to load script: " + file, e);
		}
	}

	
	boolean sshRequired() {
		return remoteUrl.startsWith("git@");
	}
	
	boolean hasSpecificIdentity() {
		return System.getenv("GIT_KEY") != null;

	}

	String getSpecificIdentity() {
		return System.getenv("GIT_KEY");

	}

}
