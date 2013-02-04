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
import static net.sourceforge.aprog.tools.Tools.cast;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import static net.sourceforge.aprog.tools.Tools.getThisPackagePath;
import glj.demo.Demo.MouseHandler;
import glj.demo.StandardScene.PrimitiveRenderer;
import glj.demo.StandardScene.Renderer;
import glj.demo.TabularDataPlotter.MainPanel.ValueGetter.ColorGetter;
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
import java.util.ArrayList;
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
			
			final JTextField xyzTextField = new JTextField("$0 $1 $2");
			final JTextField colorTextField = new JTextField("rgb $0 $1 $2");
			
			context.set("XYZ_TEXT_FIELD", xyzTextField);
			context.set("COLOR_TEXT_FIELD", colorTextField);
			
			glPanel.add(horizontalBox(new JLabel("XYZ:"), xyzTextField, new JLabel("Color:"), colorTextField), BorderLayout.NORTH);
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
					final DataConfigurator oldDataConfigurator = cast(DataConfigurator.class, dataAndGL.getLeftComponent());
					
					if (oldDataConfigurator != null) {
						oldDataConfigurator.stopTimer();
					}
					
					final DataConfigurator dataConfigurator = new DataConfigurator(context, model);
					
					dataAndGL.setLeftComponent(dataConfigurator);
					
					dataConfigurator.setModelOutOfDate(true);
				}
				
			});
			
			final ActionListener dataConfiguratorNotifier = new ActionListener() {
				
				@Override
				public final void actionPerformed(final ActionEvent event) {
					final DataConfigurator dataConfigurator = cast(DataConfigurator.class, dataAndGL.getLeftComponent());
					
					if (dataConfigurator != null) {
						dataConfigurator.setModelOutOfDate(true);
					}
				}
				
			};
			
			xyzTextField.addActionListener(dataConfiguratorNotifier);
			colorTextField.addActionListener(dataConfiguratorNotifier);
		}
		
		/**
		 * @author codistmonk (creation 2013-02-04)
		 *
		 * @param <T>
		 */
		public static interface ValueGetter<T> {
			
			public abstract double get(T object);
			
			/**
			 * @author codistmonk (creation 2013-03-04)
			 */
			public static final class Constant<T> implements ValueGetter<T> {
				
				private final double value;
				
				public Constant(final double value) {
					this.value = value;
				}
				
				@Override
				public final double get(final T object) {
					return this.value;
				}
				
			}
			
			/**
			 * @author codistmonk (creation 2013-03-04)
			 *
			 * @param <T>
			 */
			public static interface ColorGetter<T> {
				
				public abstract Color get(T object);
				
				/**
				 * @author codistmonk (creation 2013-03-04)
				 *
				 * @param <T>
				 */
				public static final class RGBAColorGetter<T> implements ColorGetter<T> {
					
					private final ValueGetter<T> redGetter;
					
					private final ValueGetter<T> greenGetter;
					
					private final ValueGetter<T> blueGetter;
					
					private final ValueGetter<T> alphaGetter;
					
					public RGBAColorGetter(final ValueGetter<T> redGetter,
							final ValueGetter<T> greenGetter,
							final ValueGetter<T> blueGetter,
							final ValueGetter<T> alphaGetter) {
						this.redGetter = redGetter;
						this.greenGetter = greenGetter;
						this.blueGetter = blueGetter;
						this.alphaGetter = alphaGetter;
					}
					
					@Override
					public final Color get(final T object) {
						return new Color(
								(int) this.redGetter.get(object),
								(int) this.greenGetter.get(object),
								(int) this.blueGetter.get(object),
								(int) this.alphaGetter.get(object));
					}
					
				}
				
			}
			
			/**
			 * @author codistmonk (creation 2013-03-04)
			 */
			public static final class ArrayElement implements ValueGetter<double[]> {
				
				private final int index;
				
				public ArrayElement(final int index) {
					this.index = index;
				}
				
				@Override
				public final double get(final double[] array) {
					return array[this.index];
				}
				
			}
			
		}
		
		public static final double normalize(final double value, final double minimum, final double maximum) {
			if (value <= minimum) {
				return 0.0;
			}
			
			if (maximum <= value) {
				return 1.0;
			}
			
			return (value - minimum) / (maximum - minimum);
		}
		
		public static final double denormalize(final double value, final double minimum, final double maximum) {
			if (value <= 0.0) {
				return minimum;
			}
			
			if (1.0 <= value) {
				return maximum;
			}
			
			return minimum + value * (maximum - minimum);
		}
		
		/**
		 * @author codistmonk (creation 2013-01-21)
		 */
		public static final class DataConfigurator extends JPanel {
			
			private final Context context;
			
			private final TabularDoubleData model;
			
			private final Timer timer;
			
			private boolean modelOutOfDate;
			
			private final List<JTextField> names;
			
			private final List<JTextField> roundings;
			
			private final List<JCheckBox> groupings;
			
			private final List<JTextField> sortings;
			
			public DataConfigurator(final Context context, final TabularDoubleData model) {
				super(new GridLayout(1, 1 + model.getColumnCount()));
				this.context = context;
				this.model = model;
				
				final int columnCount = model.getColumnCount();
				
				this.names = new ArrayList<JTextField>(columnCount);
				this.roundings = new ArrayList<JTextField>(columnCount);
				this.groupings = new ArrayList<JCheckBox>(columnCount);
				this.sortings = new ArrayList<JTextField>(columnCount);
				
				this.add(new JLabel("Name"));
				
				for (final Column column : model.getColumns()) {
					final JTextField nameTextField = new JTextField(column.getName());
					
					this.names.add(nameTextField);
					this.add(nameTextField);
				}
				
				final boolean showPlannedFeatures = false;
				
				if (showPlannedFeatures) {
					this.add(new JLabel("Rounding"));
					
					for (final Column column : model.getColumns()) {
						final JTextField roundingTextField = new JTextField("" + column.getRounding());
						
						this.roundings.add(roundingTextField);
						this.add(roundingTextField);
					}
					
					this.add(new JLabel("Grouping"));
					
					for (final Column column : model.getColumns()) {
						final JCheckBox groupingCheckBox = new JCheckBox();
						
						this.groupings.add(groupingCheckBox);
						this.add(groupingCheckBox);
					}
					
					this.add(new JLabel("Sorting"));
					
					for (final Column column : model.getColumns()) {
						final JTextField sortingTextField = new JTextField();
						
						this.sortings.add(sortingTextField);
						this.add(sortingTextField);
					}
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
			
			public final List<JTextField> getNames() {
				return this.names;
			}
			
			public final List<JTextField> getRoundings() {
				return this.roundings;
			}
			
			public final List<JCheckBox> getGroupings() {
				return this.groupings;
			}
			
			public final List<JTextField> getSortings() {
				return this.sortings;
			}
			
			public final ValueGetter<double[]> getGetter(final String name) {
				final int index = this.getIndex(name);
				
				if (0 <= index) {
					return new ValueGetter.ArrayElement(index);
				}
				
				try {
					return new ValueGetter.Constant<double[]>(Double.parseDouble(name));
				} catch (final Exception exception) {
					debugPrint(exception);
				}
				
				return new ValueGetter.Constant<double[]>(0.0);
			}
			
			public final int getIndex(final String name) {
				int index = 0;
				
				for (final JTextField nameTextField : this.getNames()) {
					if (nameTextField.getText().equals(name)) {
						return index;
					}
					
					++index;
				}
				
				return -1;
			}
			
			public final synchronized void maybeUpdateModel() {
				if (this.isModelOutOfDate()) {
					this.setModelOutOfDate(false);
					
					try {
						this.updateModelAndView();
					} catch (final Exception exception) {
						exception.printStackTrace();
					}
					
					final GLCanvas glCanvas = this.context.get("GL_CANVAS");
					
					glCanvas.repaint();
				}
			}
			
			private final void updateModelAndView() {
				debugPrint();
				final Map<String, Renderer> renderers = this.context.get("RENDERERS");
				final PrimitiveRenderer dataPoints = (PrimitiveRenderer) renderers.get("dataPoints");
				final JTextField xyzTextField = this.context.get("XYZ_TEXT_FIELD");
				final JTextField colorTextField = this.context.get("COLOR_TEXT_FIELD");
				
				this.model.updateView();
				
				final String[] xyzNames = xyzTextField.getText().split("\\s+");
				final ValueGetter<double[]> xGetter = this.getGetter(xyzNames[0]);
				final ValueGetter<double[]> yGetter = this.getGetter(xyzNames[1]);
				final ValueGetter<double[]> zGetter = this.getGetter(xyzNames[2]);
				final String[] colorFunctionAndNames = colorTextField.getText().split("\\s+");
				final int channelCount = colorFunctionAndNames.length - 1;
				final ColorGetter<double[]> colorGetter;
				
				if (channelCount == 1) {
					throw new RuntimeException("TODO");
				} else if (channelCount == 3) {
					if ("rgb".equals(colorFunctionAndNames[0].toLowerCase(Locale.ENGLISH))) {
						colorGetter = new ColorGetter.RGBAColorGetter<double[]>(
								this.getGetter(colorFunctionAndNames[1]),
								this.getGetter(colorFunctionAndNames[2]),
								this.getGetter(colorFunctionAndNames[3]),
								new ValueGetter.Constant<double[]>(1.0));
					} else {
						throw new IllegalArgumentException("Invalid color function: " + colorFunctionAndNames[0]);
					}
				} else if (channelCount == 4) {
					if ("rgba".equals(colorFunctionAndNames[0].toLowerCase(Locale.ENGLISH))) {
						colorGetter = new ColorGetter.RGBAColorGetter<double[]>(
								this.getGetter(colorFunctionAndNames[1]),
								this.getGetter(colorFunctionAndNames[2]),
								this.getGetter(colorFunctionAndNames[3]),
								this.getGetter(colorFunctionAndNames[4]));
					} else {
						throw new IllegalArgumentException("Invalid color function: " + colorFunctionAndNames[0]);
					}
				} else {
					throw new IllegalArgumentException("Invalid channel count: " + channelCount);
				}
				
				final Collection<ViewRow> viewRows = this.model.getViewRows();
				final int rowCount = viewRows.size();
				final float[] vertices = new float[3 * rowCount];
				final float[] colors = new float[4 * rowCount];
				int vertexIndex = 0;
				final float[] rgba = new float[4];
				
				for (final ViewRow row : viewRows) {
					vertices[3 * vertexIndex + 0] = (float) denormalize(normalize(xGetter.get(row.getDataRow()), 0.0, 255.0), -128.0, +127.0);
					vertices[3 * vertexIndex + 1] = (float) denormalize(normalize(yGetter.get(row.getDataRow()), 0.0, 255.0), -128.0, +127.0);
					vertices[3 * vertexIndex + 2] = (float) denormalize(normalize(zGetter.get(row.getDataRow()), 0.0, 255.0), -128.0, +127.0);
					
					colorGetter.get(row.getDataRow()).getColorComponents(rgba);
					
					colors[4 * vertexIndex + 0] = rgba[0];
					colors[4 * vertexIndex + 1] = rgba[1];
					colors[4 * vertexIndex + 2] = rgba[2];
					colors[4 * vertexIndex + 3] = rgba[3];
					++vertexIndex;
				}
				
				final GLCanvas glCanvas = this.context.get("GL_CANVAS");
				
				glCanvas.invoke(false, new GLRunnable() {
					
					@Override
					public final boolean run(final GLAutoDrawable drawable) {
						dataPoints.getLocations().update(0, vertices);
						dataPoints.getColors().update(0, colors);
						
						return false;
					}
					
				});
			}
			
			public final synchronized boolean isModelOutOfDate() {
				return this.modelOutOfDate;
			}
			
			public final synchronized void setModelOutOfDate(final boolean modelOutOfDate) {
				this.modelOutOfDate = modelOutOfDate;
			}
			
			public final synchronized void stopTimer() {
				this.timer.stop();
			}
			
			@Override
			protected final void finalize() throws Throwable {
				try {
					this.stopTimer();
					timers.remove(this.timer);
				} finally {
					super.finalize();
				}
			}
			
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
