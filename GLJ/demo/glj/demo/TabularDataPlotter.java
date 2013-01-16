package glj.demo;

import static javax.swing.SwingUtilities.invokeLater;
import static net.sourceforge.aprog.af.AFTools.newAboutItem;
import static net.sourceforge.aprog.af.AFTools.newPreferencesItem;
import static net.sourceforge.aprog.af.AFTools.newQuitItem;
import static net.sourceforge.aprog.i18n.Messages.setMessagesBase;
import static net.sourceforge.aprog.swing.SwingTools.horizontalBox;
import static net.sourceforge.aprog.swing.SwingTools.menuBar;
import static net.sourceforge.aprog.swing.SwingTools.packAndCenter;
import static net.sourceforge.aprog.swing.SwingTools.scrollable;
import static net.sourceforge.aprog.swing.SwingTools.useSystemLookAndFeel;
import static net.sourceforge.aprog.swing.SwingTools.verticalSplit;
import static net.sourceforge.aprog.swing.SwingTools.I18N.menu;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import static net.sourceforge.aprog.tools.Tools.getThisPackagePath;

import glj.Scene;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLProfile.ShutdownType;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.sourceforge.aprog.af.AFConstants;
import net.sourceforge.aprog.af.AFMainFrame;
import net.sourceforge.aprog.af.AFTools;
import net.sourceforge.aprog.af.AbstractAFAction;
import net.sourceforge.aprog.af.MacOSXTools;
import net.sourceforge.aprog.context.Context;
import net.sourceforge.aprog.events.Variable;
import net.sourceforge.aprog.events.Variable.ValueChangedEvent;
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
	
	public static final void open(final Context context, final File file) {
		try {
			final Scanner scanner = new Scanner(file);
			final DefaultTableModel dataModel = new DefaultTableModel();
			int columnCount = 0;
			
			scanner.useLocale(Locale.ENGLISH);
			
			while (scanner.hasNext()) {
				final Object[] row = toObjects(toDoubles(scanner.nextLine().split("\\p{Blank}+")));
				
				if (columnCount == 0) {
					columnCount = row.length;
					
					for (int i = 1; i <= columnCount; ++i) {
						dataModel.addColumn("$" + i);
					}
				}
				
				dataModel.addRow(row);
			}
			
			context.set("FILE", file);
			context.set("DATA_MODEL", dataModel);
		} catch (final FileNotFoundException exception) {
			exception.printStackTrace();
		}
	}
	
	public static final Object[] toObjects(final double... values) {
		final int n = values.length;
		final Object[] result = new Object[n];
		
		for (int i = 0; i < n; ++i) {
			result[i] = values[i];
		}
		
		return result;
	}
	
	public static final double[] toDoubles(final String... strings) {
		final int n = strings.length;
		final double[] result = new double[n];
		
		for (int i = 0; i < n; ++i) {
			result[i] = Double.parseDouble(strings[i]);
		}
		
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
			
			glCanvas.setPreferredSize(new Dimension(800, 600));
			glCanvas.setMinimumSize(new Dimension(1, 1));
			
			final JPanel glPanel = new JPanel(new BorderLayout());
			
			glPanel.add(horizontalBox(new JLabel("X:"), new JTextField(), new JLabel("Y:"), new JTextField(), new JLabel("Z:"), new JTextField(),
					new JLabel("Color:"), new JTextField()), BorderLayout.NORTH);
			glPanel.add(glCanvas, BorderLayout.CENTER);
			
			final JTable dataTable = new JTable();
			
			this.add(verticalSplit(
					scrollable(dataTable),
					glPanel
			), BorderLayout.CENTER);
			
			this.setDropTarget(new HighlightBorderDropTarget() {
				
				@Override
				protected final void processDrop(final DropTargetDropEvent dtde) {
					try {
						open(context, SwingTools.getFiles(dtde).get(0));
					} catch (final Exception exception) {
						exception.printStackTrace();
					}
				}
				
			});
			
			context.set("DATA_MODEL", dataTable.getModel(), TableModel.class);
			final Variable<TableModel> dataModelVariable = context.getVariable("DATA_MODEL");
			dataModelVariable.addListener(new Variable.Listener<TableModel>() {
				
				@Override
				public final void valueChanged(final ValueChangedEvent<TableModel, ?> event) {
					final DefaultTableModel dataModel = (DefaultTableModel) event.getNewValue();
					debugPrint("rowCount:", dataModel.getRowCount(), "columnCount:", dataModel.getColumnCount());
					dataTable.setModel(dataModel);
					dataModel.fireTableDataChanged();
				}
				
			});
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
	
	/**
	 * @author codistmonk (creation 2013-01-14)
	 */
	public static abstract class HighlightBorderDropTarget extends DropTarget {
		
		private Border backup;
		
		@Override
		public final synchronized void dragEnter(final DropTargetDragEvent dtde) {
			final JComponent component = (JComponent) this.getComponent();
			
			this.backup = component.getBorder();
			
			component.setBorder(BorderFactory.createLineBorder(Color.RED));
		}
		
		@Override
		public final synchronized void dragExit(final DropTargetEvent dte) {
			final JComponent component = (JComponent) this.getComponent();
			
			component.setBorder(this.backup);
			
			this.backup = null;
		}
		
		@Override
		public final synchronized void drop(final DropTargetDropEvent dtde) {
			this.dragExit(dtde);
			
			this.processDrop(dtde);
		}
		
		protected void processDrop(final DropTargetDropEvent dtde) {
			// NOP
		}
		
	}
	
}
