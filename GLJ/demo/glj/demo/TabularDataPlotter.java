package glj.demo;

import static java.util.Collections.synchronizedList;
import static javax.swing.SwingUtilities.invokeLater;
import static net.sourceforge.aprog.af.AFTools.newAboutItem;
import static net.sourceforge.aprog.af.AFTools.newPreferencesItem;
import static net.sourceforge.aprog.af.AFTools.newQuitItem;
import static net.sourceforge.aprog.i18n.Messages.setMessagesBase;
import static net.sourceforge.aprog.swing.SwingTools.horizontalBox;
import static net.sourceforge.aprog.swing.SwingTools.menuBar;
import static net.sourceforge.aprog.swing.SwingTools.packAndCenter;
import static net.sourceforge.aprog.swing.SwingTools.useSystemLookAndFeel;
import static net.sourceforge.aprog.swing.SwingTools.verticalSplit;
import static net.sourceforge.aprog.swing.SwingTools.I18N.menu;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import static net.sourceforge.aprog.tools.Tools.getThisPackagePath;
import glj.demo.Demo.MouseHandler;
import glj.demo.StandardScene.PrimitiveRenderer;
import glj.demo.StandardScene.Renderer;
import glj.demo.TabularDoubleData.Column;
import glj.demo.TabularDoubleData.ViewRow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLRunnable;
import javax.media.opengl.GLProfile.ShutdownType;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.Border;

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
	
	static final List<Timer> timers = synchronizedList(new LinkedList<Timer>());
	
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
				
				while (!timers.isEmpty()) {
					timers.remove(0).stop();
				}
			}
			
		};
		
		result.set(AFConstants.Variables.MAIN_MENU_BAR, menuBar(
				menu("Application",
						newAboutItem(result),
						null,
						newPreferencesItem(result),
						null,
						newQuitItem(result))));
		
		result.set("DATA", null, TabularDoubleData.class);
		
		return result;
	}
	
	public static final void open(final Context context, final File file) {
		try {
			final Scanner scanner = new Scanner(file);
			
			scanner.useLocale(Locale.ENGLISH);
			
			final TabularDoubleData data = new TabularDoubleData();
			
			while (scanner.hasNext()) {
				data.addDataRow(toDoubles(scanner.nextLine().split("\\p{Blank}+")));
			}
			
			debugPrint(data.getRowCount(), data.getColumnCount());
			
			context.set("DATA", data);
			context.set("FILE", file);
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
		final StandardScene scene = new StandardScene(context);
		
		context.set("GL_CANVAS", glCanvas);
		context.set("SCENE", scene);
		
		glCanvas.addGLEventListener(scene);
		
		new MouseHandler(scene).addTo(glCanvas);
		
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
			
			final JSplitPane dataAndGL = verticalSplit(
					new JLabel("NO DATA"),
					glPanel
			);
			this.add(dataAndGL, BorderLayout.CENTER);
			
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
			
			final Variable<TabularDoubleData> dataModelVariable = context.getVariable("DATA");
			
			dataModelVariable.addListener(new Variable.Listener<TabularDoubleData>() {
				
				@Override
				public final void valueChanged(final ValueChangedEvent<TabularDoubleData, ?> event) {
					final TabularDoubleData model = event.getNewValue();
					dataAndGL.setLeftComponent(new DataConfigurator(context, model));
					
					final Map<String, Renderer> renderers = context.get("RENDERERS");
					final PrimitiveRenderer dataPoints = (PrimitiveRenderer) renderers.get("dataPoints");
					
					model.updateView();
					
					final Collection<ViewRow> viewRows = model.getViewRows();
					final int rowCount = viewRows.size();
					final float[] vertices = new float[3 * rowCount];
					final float[] colors = new float[4 * rowCount];
					int vertexIndex = 0;
					
					for (final ViewRow row : viewRows) {
						vertices[3 * vertexIndex + 0] = (float) row.getDataRow()[0];
						vertices[3 * vertexIndex + 1] = (float) row.getDataRow()[1];
						vertices[3 * vertexIndex + 2] = (float) row.getDataRow()[2];
						colors[4 * vertexIndex + 0] = (float) row.getDataRow()[0];
						colors[4 * vertexIndex + 1] = (float) row.getDataRow()[1];
						colors[4 * vertexIndex + 2] = (float) row.getDataRow()[2];
						colors[4 * vertexIndex + 3] = +1.0F;
						++vertexIndex;
					}
					
					final GLCanvas glCanvas = context.get("GL_CANVAS");
					
					glCanvas.invoke(false, new GLRunnable() {
						
						@Override
						public final boolean run(final GLAutoDrawable drawable) {
							dataPoints.getLocations().update(0, vertices);
							dataPoints.getColors().update(0, colors);
							
							return false;
						}
						
					});
				}
				
			});
		}
		
		/**
		 * @author codistmonk (creation 2013-01-21)
		 */
		public static final class DataConfigurator extends JPanel {
			
			private final Context context;
			
			private final TabularDoubleData model;
			
			private final Timer timer;
			
			private boolean modelOutOfDate;
			
			public DataConfigurator(final Context context, final TabularDoubleData model) {
				super(new GridLayout(4, 1 + model.getColumnCount()));
				this.context = context;
				this.model = model;
				
				this.add(new JLabel("Name"));
				
				for (final Column column : model.getColumns()) {
					this.add(new JTextField(column.getName()));
				}
				
				this.add(new JLabel("Rounding"));
				
				for (final Column column : model.getColumns()) {
					this.add(new JTextField("" + column.getRounding()));
				}
				
				this.add(new JLabel("Grouping"));
				
				for (final Column column : model.getColumns()) {
					this.add(new JCheckBox());
				}
				
				this.add(new JLabel("Sorting"));
				
				for (final Column column : model.getColumns()) {
					this.add(new JTextField());
				}
				
				this.timer = new Timer(500, new ActionListener() {
					
					@Override
					public final void actionPerformed(final ActionEvent event) {
						DataConfigurator.this.maybeUpdateModel();
					}
					
				});
				
				this.timer.setRepeats(true);
				this.timer.setCoalesce(true);
				this.timer.start();
				
				timers.add(this.timer);
			}
			
			public final synchronized void maybeUpdateModel() {
				if (this.isModelOutOfDate()) {
					this.setModelOutOfDate(false);
					this.model.updateView();
					
					final GLCanvas glCanvas = this.context.get("GL_CANVAS");
					
					glCanvas.repaint();
				}
			}
			
			public final synchronized boolean isModelOutOfDate() {
				return this.modelOutOfDate;
			}
			
			public final synchronized void setModelOutOfDate(final boolean modelOutOfDate) {
				this.modelOutOfDate = modelOutOfDate;
			}
			
			@Override
			protected final void finalize() throws Throwable {
				try {
					this.timer.stop();
					timers.remove(this.timer);
				} finally {
					super.finalize();
				}
			}
			
		}
		
	}
	
//	/**
//	 * @author codistmonk (creation 2013-01-14)
//	 */
//	public static final class Scene extends glj.Scene {
//		
//		private final Context context;
//		
//		public Scene(final Context context) {
//			this.context = context;
//		}
//		
//		@Override
//		protected final void initialize() {
//			super.initialize();
//			
//			debugPrint("TODO");
//		}
//		
//		@Override
//		protected final void display() {
//			super.display();
//			
//			debugPrint("TODO");
//		}
//		
//	}
	
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
