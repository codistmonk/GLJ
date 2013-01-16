package glj.demo;

import glj.LauncherTools;

/**
 * @author codistmonk (creation 2013-01-14)
 */
public final class Launcher {
	
	/**
	 * @param arguments
	 * <br>Maybe null
	 */
	public static final void main(final String[] arguments) {
		LauncherTools.launch(null, TabularDataPlotter.class, arguments);
	}
	
}
