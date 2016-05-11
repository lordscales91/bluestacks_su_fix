package io.github.lordscales91.bstktmproot;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class ShellUtils extends Shell {
	/**
	 * This class extends the Shell class from libsuperuser library.
	 * It is used to find BlueStacks legacy su and execute commands on it
	 */
	public static class BSTK {
		// For now we will assume the su is in the expected location
		public static final String bstkShell = "/system/xbin/bstk/su";
	
		public static List<String> run(String... commands) {
			return Shell.run(bstkShell, commands, null, false);
		}
		
		public static List<String> run(List<String> commands) {
			return run(commands.toArray(new String[]{}));
		}
		
		public static boolean available() {
			List<String> ret = run(Shell.availableTestCommands);
			return Shell.parseAvailableResult(ret, true);
		}
	}
}
