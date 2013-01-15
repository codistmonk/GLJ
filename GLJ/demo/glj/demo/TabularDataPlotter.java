package glj.demo;

import static javax.swing.SwingUtilities.invokeLater;
import static net.sourceforge.aprog.af.AFTools.newAboutItem;
import static net.sourceforge.aprog.af.AFTools.newPreferencesItem;
import static net.sourceforge.aprog.af.AFTools.newQuitItem;
import static net.sourceforge.aprog.i18n.Messages.setMessagesBase;
import static net.sourceforge.aprog.swing.SwingTools.menuBar;
import static net.sourceforge.aprog.swing.SwingTools.packAndCenter;
import static net.sourceforge.aprog.swing.SwingTools.useSystemLookAndFeel;
import static net.sourceforge.aprog.swing.SwingTools.verticalSplit;
import static net.sourceforge.aprog.swing.SwingTools.I18N.menu;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import static net.sourceforge.aprog.tools.Tools.getThisPackagePath;

import glj.Scene;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLProfile.ShutdownType;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.sourceforge.aprog.af.AFConstants;
import net.sourceforge.aprog.af.AFMainFrame;
import net.sourceforge.aprog.af.AFTools;
import net.sourceforge.aprog.af.AbstractAFAction;
import net.sourceforge.aprog.af.MacOSXTools;
import net.sourceforge.aprog.context.Context;
import net.sourceforge.aprog.swing.SwingTools;
import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2013-01-14)
 */
public final class TabularDataPlotter {
	
	private TabularDataPlotter() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * {@value}.
	 */
	public static final String APPLICATION_NAME = "Tabular Data Plotter";
	
	/**
	 * {@value}.
	 */
	public static final String APPLICATION_VERSION = "0.1.0";
	
	/**
	 * {@value}.
	 */
	public static final String APPLICATION_COPYRIGHT = "(c) 2013 Codist Monk";
	
	/**
	 * {@value}.
	 */
	public static final String APPLICATION_ICON_PATH = "glj/demo/tabular_data_plotter_icon.png";
	
	static {
		GLProfile.initSingleton();
	}
	
	public static final Context newContext() {
		final Context result = AFTools.newContext();
		
		result.set(AFConstants.Variables.APPLICATION_NAME, APPLICATION_NAME);
		result.set(AFConstants.Variables.APPLICATION_VERSION, APPLICATION_VERSION);
		result.set(AFConstants.Variables.APPLICATION_COPYRIGHT, APPLICATION_COPYRIGHT);
		result.set(AFConstants.Variables.APPLICATION_ICON_PATH, APPLICATION_ICON_PATH);
		
		new AbstractAFAction(result, AFConstants.Variables.ACTIONS_QUIT) {
			
			@Override
			public final void perform() {
				final AFMainFrame mainFrame = result.get(AFConstants.Variables.MAIN_FRAME);
				
				mainFrame.dispose();
				
				GLProfile.shutdown(ShutdownType.COMPLETE);
			}
			
		};
		
		result.set(AFConstants.Variables.MAIN_MENU_BAR, menuBar(
				menu("Application",
						newAboutItem(result),
						null,
						newPreferencesItem(result),
						null,
						newQuitItem(result))));
		
		return result;
	}
	
	/**
	 * @param arguments
	 * <br>unused
	 */
	public static final void main(final String[] arguments) {
		MacOSXTools.setupUI(APPLICATION_NAME, APPLICATION_ICON_PATH);
		useSystemLookAndFeel();
		setMessagesBase(getThisPackagePath() + "i18n/Messages");
		
		
		invokeLater(new Runnable() {
			
			@Override
			public final void run() {
				final Context context = newContext();
				final AFMainFrame mainFrame = AFMainFrame.newMainFrame(context);
				
				mainFrame.setContentPane(new MainPanel(context));
				
				mainFrame.setPreferredSize(new Dimension(800, 600));
				
				packAndCenter(mainFrame).setVisible(true);
			}
			
		});
	}
	
	public static final GLCanvas newGLCanvas(final Context context) {
		final GLProfile glProfile = GLProfile.get(GLProfile.GL2GL3);
		final GLCapabilities glCapabilities = new GLCapabilities(glProfile);
		final GLCanvas glCanvas = new GLCanvas(glCapabilities);
		
		context.set("GL_CANVAS", glCanvas);
		
		glCanvas.addGLEventListener(new Scene(context));
		
		return glCanvas;
	}
	
	/**
	 * @author codistmonk (creation 2013-01-14)
	 */
	public static final class MainPanel extends JPanel {
		
		private final Context context;
		
		public MainPanel(final Context context) {
			super(new BorderLayout());
			this.context = context;
			
			final GLCanvas glCanvas = newGLCanvas(context);
			
			glCanvas.setMinimumSize(new Dimension(1, 1));
			
			final JPanel glPanel = new JPanel(new BorderLayout());
			
			glPanel.add(glCanvas, BorderLayout.CENTER);
			
			this.add(verticalSplit(new JLabel("TODO"), glPanel), BorderLayout.CENTER);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2013-01-14)
	 */
	public static final class Scene extends glj.Scene {
		
		private final Context context;
		
		public Scene(final Context context) {
			this.context = context;
		}
		
		@Override
		protected final void initialize() {
			super.initialize();
			
			debugPrint("TODO");
		}
		
		@Override
		protected final void display() {
			super.display();
			
			debugPrint("TODO");
		}
		
	}
	
}
