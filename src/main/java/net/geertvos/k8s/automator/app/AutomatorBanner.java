package net.geertvos.k8s.automator.app;

import java.io.PrintStream;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

public class AutomatorBanner implements Banner {

	@Override
	public void printBanner(Environment arg0, Class<?> arg1, PrintStream arg2) {
		arg2.println("\n" + 
				"░█████╗░██╗░░░██╗████████╗░█████╗░███╗░░░███╗░█████╗░████████╗░█████╗░██████╗░\n" + 
				"██╔══██╗██║░░░██║╚══██╔══╝██╔══██╗████╗░████║██╔══██╗╚══██╔══╝██╔══██╗██╔══██╗\n" + 
				"███████║██║░░░██║░░░██║░░░██║░░██║██╔████╔██║███████║░░░██║░░░██║░░██║██████╔╝\n" + 
				"██╔══██║██║░░░██║░░░██║░░░██║░░██║██║╚██╔╝██║██╔══██║░░░██║░░░██║░░██║██╔══██╗\n" + 
				"██║░░██║╚██████╔╝░░░██║░░░╚█████╔╝██║░╚═╝░██║██║░░██║░░░██║░░░╚█████╔╝██║░░██║\n" + 
				"╚═╝░░╚═╝░╚═════╝░░░░╚═╝░░░░╚════╝░╚═╝░░░░░╚═╝╚═╝░░╚═╝░░░╚═╝░░░░╚════╝░╚═╝░░╚═╝");
	}
}