package glj.demo;

import static glj.GLJTools.ORIGIN;
import static glj.GLJTools.UNIT_Y;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import static net.sourceforge.aprog.swing.SwingTools.packAndCenter;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import static net.sourceforge.aprog.tools.Tools.unchecked;
import glj.GLJTools;
import glj.Scene;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDebugListener;
import javax.media.opengl.GLDebugMessage;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import net.sourceforge.aprog.tools.CommandLineArgumentsParser;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2012-01-12)
 */
public final class Demo {
	
	private Demo() {
		throw new IllegalInstantiationException();
	}
	
	public static final int[] parseInts(final String... strings) {
		final int n = strings.length;
		final int[] result = new int[n];
		
		for (int i = 0; i < n; ++i) {
			result[i] = Integer.parseInt(strings[i]);
		}
		
		return result;
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Must not be null
	 */
	public static final void main(final String[] commandLineArguments) {
		final CommandLineArgumentsParser arguments = new CommandLineArgumentsParser(commandLineArguments);
		final GLProfile glProfile = GLProfile.get(GLProfile.GL2GL3);
		final GLCapabilities glCapabilities = new GLCapabilities(glProfile);
		final String dataFilePath = arguments.get("file", "");
		final int[] xyz = arguments.get("xyz", 1, 2, 3);
		final int v = arguments.get("v", 0)[0];
		final String gradient = arguments.get("gradient", "gray");
		
		invokeLater(new Runnable() {
			
			@Override
			public final void run() {
				final JFrame frame = new JFrame("GLJ");
				final GLCanvas glCanvas = new GLCanvas(glCapabilities);
				final DemoScene scene = new DemoScene(new DataLoader(dataFilePath, xyz, v, gradient));
				
				glCanvas.addGLEventListener(scene);
				new MouseHandler(scene).addTo(glCanvas);
				
				frame.add(glCanvas, BorderLayout.CENTER);
				frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				frame.setPreferredSize(new Dimension(DemoScene.Camera.DEFAULT_VIEWPORT_WIDTH, DemoScene.Camera.DEFAULT_VIEWPORT_HEIGHT));
				
				packAndCenter(frame).setVisible(true);
			}
			
		});
	}
	
	static {
		GLProfile.initSingleton();
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public static final class MouseHandler extends MouseAdapter {
		
		private double cameraAzimuth;
		
		private double cameraElevation;
		
		private double cameraDistanceFromTarget;
		
		private final Point mouse;
		
		private final Scene scene;
		
		public MouseHandler(final Scene scene) {
			this.mouse = new Point();
			this.scene = scene;
		}
		
		public final void addTo(final Component component) {
			component.addMouseListener(this);
			component.addMouseMotionListener(this);
			component.addMouseWheelListener(this);
		}
		
		@Override
		public final void mousePressed(final MouseEvent event) {
			this.mouse.setLocation(event.getPoint());
			this.retrieveSceneCameraParameters();
		}
		
		@Override
		public final void mouseDragged(final MouseEvent event) {
			final int dx = event.getX() - this.mouse.x;
			final int dy = event.getY() - this.mouse.y;
			this.cameraAzimuth -= dx * PI / 512.0;
			this.cameraElevation += dy * PI / 512.0;
			this.mouse.setLocation(event.getPoint());
			
			this.updateSceneCamera();
		}
		
		@Override
		public final void mouseWheelMoved(final MouseWheelEvent event) {
			this.retrieveSceneCameraParameters();
			
			if (event.getWheelRotation() < 0) {
				this.cameraDistanceFromTarget *= 1.1;
			} else {
				this.cameraDistanceFromTarget /= 1.1;
			}
			
			this.updateSceneCamera();
		}
		
		private final void retrieveSceneCameraParameters() {
			final Point3f cameraLocation = GLJTools.getCameraLocation(this.scene.getDefaultCamera());
			this.cameraAzimuth = atan2(cameraLocation.x, cameraLocation.z);
			this.cameraDistanceFromTarget = length(cameraLocation);
		}
		
		private final void updateSceneCamera() {
			this.scene.getDefaultCamera().setPosition(new Point3f(
					(float) (this.cameraDistanceFromTarget * cos(this.cameraElevation) * sin(this.cameraAzimuth)),
					(float) (this.cameraDistanceFromTarget * sin(this.cameraElevation)),
					(float) (this.cameraDistanceFromTarget * cos(this.cameraElevation) * cos(this.cameraAzimuth))
			), ORIGIN, UNIT_Y);
			
			this.scene.getDrawable().display();
		}
		
		public static final double length(final Tuple3f t) {
			return sqrt(t.x * t.x + t.y * t.y + t.z * t.z);
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2012-01-12)
	 */
	public static final class DemoScene extends Scene {
		
		/**
		 * @author codistmonk (creation 2012-01-12)
		 */
		public abstract interface Renderer {
			
			public abstract void render();
			
		}
		
		/**
		 * @author codistmonk (creation 2012-01-12)
		 */
		public final class Points implements Renderer {
			
			private final ShaderProgram shaderProgram;
			
			private final VBO locations;
			
			private final VBO colors;
			
			private final Matrix4f position;
			
			public Points(final ShaderProgram shaderProgram, final VBO locations,
					final VBO colors, final Matrix4f position) {
				this.shaderProgram = shaderProgram;
				this.locations = locations;
				this.colors = colors;
				this.position = position;
			}
			
			@Override
			public final void render() {
				this.shaderProgram.setUniform("projection", DemoScene.this.getDefaultCamera().getProjection());
				this.shaderProgram.setUniform("view", DemoScene.this.getDefaultCamera().getPosition());
				this.shaderProgram.setUniform("model", this.position);
				this.shaderProgram.bindAttribute("vertexLocation", this.locations);
				this.shaderProgram.bindAttribute("vertexColor", this.colors);
				this.shaderProgram.use();
				DemoScene.this.getGL().glDrawArrays(GL.GL_POINTS, 0, this.locations.getComponentCount()); DemoScene.this.debugGL();
				this.shaderProgram.unuse();
			}
			
		}
		
		/**
		 * @author codistmonk (creation 2012-01-12)
		 */
		public final class Segment implements Renderer {
			
			private final ShaderProgram shaderProgram;
			
			private final VBO locations;
			
			private final VBO colors;
			
			private final Matrix4f position;
			
			public Segment(final ShaderProgram shaderProgram, final VBO locations,
					final VBO colors, final Matrix4f position) {
				this.shaderProgram = shaderProgram;
				this.locations = locations;
				this.colors = colors;
				this.position = position;
			}
			
			@Override
			public final void render() {
				this.shaderProgram.setUniform("projection", DemoScene.this.getDefaultCamera().getProjection());
				this.shaderProgram.setUniform("view", DemoScene.this.getDefaultCamera().getPosition());
				this.shaderProgram.setUniform("model", this.position);
				this.shaderProgram.bindAttribute("vertexColor", this.colors);
				this.shaderProgram.bindAttribute("vertexLocation", this.locations);
				this.shaderProgram.use();
				DemoScene.this.getGL().glDrawArrays(GL.GL_LINES, 0, this.locations.getComponentCount()); DemoScene.this.debugGL();
				this.shaderProgram.unuse();
			}
			
		}
		
		private final DataLoader dataLoader;
		
		private final Collection<Renderer> renderers;
		
		public DemoScene(final DataLoader dataLoader) {
			this.dataLoader = dataLoader;
			this.renderers = new ArrayList<Renderer>();
		}
		
		@Override
		protected final void initialize() {
			super.initialize();
			
			this.getDrawable().getContext().enableGLDebugMessage(true);
			this.getDrawable().getContext().addGLDebugListener(new GLDebugListener() {
				
				@Override
				public final void messageSent(final GLDebugMessage event) {
					debugPrint(event);
				}
				
			});
			
			final Shader vertexShader = this.new Shader(GL2ES2.GL_VERTEX_SHADER)
			.appendLine("#version " + this.getGLSL().getVersion())
			.appendLine("")
			.appendLine("uniform mat4 projection;")
			.appendLine("uniform mat4 view;")
			.appendLine("uniform mat4 model;")
			.appendLine("")
			.appendLine(this.getGLSL().getAttribute() + " vec3 vertexLocation;")
			.appendLine(this.getGLSL().getAttribute() + " vec4 vertexColor;")
			.appendLine("")
			.appendLine(this.getGLSL().getVaryingOut() + " vec4 fragmentInputColor;")
			.appendLine("")
			.appendLine("void main()")
			.appendLine("{")
			.appendLine("	gl_Position = projection * view * model * vec4(vertexLocation, 1.0);")
			.appendLine("	fragmentInputColor = vertexColor;")
			.appendLine("}")
			;
			
			final Shader fragmentShader = this.new Shader(GL2ES2.GL_FRAGMENT_SHADER)
			.appendLine("#version " + this.getGLSL().getVersion())
			.appendLine("")
			.appendLine(this.getGLSL().getVaryingIn() + " vec4 fragmentInputColor;")
			.appendLine("")
			.appendLine(this.getGLSL().getDeclareFragmentOutputColor())
			.appendLine("")
			.appendLine("void main()")
			.appendLine("{")
			.appendLine("	" + this.getGLSL().getFragmentOutputColor() + " = fragmentInputColor;")
			.appendLine("}")
			;
			
			final ShaderProgram shaderProgram = this.new ShaderProgram()
			.attach(vertexShader.compile())
			.attach(fragmentShader.compile())
			.link()
			;
			
			final Matrix4f position = new Matrix4f();
			
			position.setIdentity();
			
			this.renderers.add(new Points(
					shaderProgram,
					this.new VBO(GL_FLOAT, 3).update(0, this.dataLoader.getLocations()),
					this.new VBO(GL_FLOAT, 4).update(0, this.dataLoader.getColors()),
					position
			));
			
			for (final float[] ij : new float[][] { { -128.0F, -128.0F }, { -128.0F, +127.0F }, { +127.0F, +127.0F }, { +127.0F, -128.0F } }) {
				final float i = ij[0];
				final float j = ij[1];
				final float ci = (i + 128.0F) / 255.0F;
				final float cj = (j + 128.0F) / 255.0F;
				
				this.renderers.add(new Segment(
						shaderProgram,
						this.new VBO(GL_FLOAT, 3).update(0,
								-128.0F, i, j,
								+127.0F, i, j
						),
						this.new VBO(GL_FLOAT, 4).update(0,
								+0.0F, ci, cj, +1.0F,
								+1.0F, ci, cj, +1.0F
						),
						position
				));
				
				this.renderers.add(new Segment(
						shaderProgram,
						this.new VBO(GL_FLOAT, 3).update(0,
								j, -128.0F, i,
								j, +127.0F, i
								),
						this.new VBO(GL_FLOAT, 4).update(0,
								cj, +0.0F, ci, +1.0F,
								cj, +1.0F, ci, +1.0F
								),
						position
				));
				
				this.renderers.add(new Segment(
						shaderProgram,
						this.new VBO(GL_FLOAT, 3).update(0,
								i, j, -128.0F,
								i, j, +127.0F
								),
						this.new VBO(GL_FLOAT, 4).update(0,
								ci, cj, +0.0F, +1.0F,
								ci, cj, +1.0F, +1.0F
								),
						position
				));
			}
		}
		
		@Override
		protected final void display() {
			super.display();
			
			for (final Renderer renderer : this.renderers) {
				renderer.render();
			}
		}
		
	}
	
	/**
	 * @author codistmonk (creation 2013-01-12)
	 */
	public static final class DataLoader {
		
		private final float[] locations;
		
		private final float[] colors;
		
		public DataLoader(final String dataFilePath, final int[] xyz, final int v, final String gradient) {
			if ("".equals(dataFilePath)) {
				this.locations = new float[] {
						+0.0F, +0.0F, +0.0F,
						+0.5F, +0.0F, +0.0F,
						+0.0F, +0.5F, +0.0F,
						+0.0F, +0.0F, +0.5F
				};
				this.colors = new float[] {
						+1.0F, +1.0F, +1.0F, +1.0F,
						+1.0F, +0.0F, +0.0F, +1.0F,
						+0.0F, +1.0F, +0.0F, +1.0F,
						+0.0F, +0.0F, +1.0F, +1.0F
				};
			} else {
				try {
					int elementCount = 0;
					
					{
						final Scanner scanner = new Scanner(new File(dataFilePath));
						
						while (scanner.hasNext()) {
							++elementCount;
							scanner.nextLine();
						}
					}
					
					debugPrint("xyz:", Arrays.toString(xyz), "v:", v, "gradient:", gradient);
					debugPrint("elementCount:", elementCount);
					
					{
						final Scanner scanner = new Scanner(new File(dataFilePath));
						this.locations = new float[3 * elementCount];
						this.colors = new float[4 * elementCount];
						
						int locationIndex = 0;
						int colorIndex = 0;
						
						while (scanner.hasNext()) {
							final String line = scanner.nextLine();
							
							final int[] row = parseInts(line.split("\\p{Blank}"));
							
							for (int i = 0; i < 3; ++i) {
								this.locations[locationIndex++] = row[xyz[i] - 1] - 128.0F;
							}
							
							if (v == 0) {
								for (int i = 0; i < 3; ++i) {
									this.colors[colorIndex++] = row[xyz[i] - 1] / 255.0F;
								}
							} else {
								final float c = row[v - 1] / 255.0F;
								
								for (int i = 0; i < 3; ++i) {
									this.colors[colorIndex++] = c;
								}
							}
							
							this.colors[colorIndex++] = 1.0F;
						}
					}
				} catch (final Exception exception) {
					throw unchecked(exception);
				}
			}
		}
		
		public final float[] getLocations() {
			return this.locations;
		}
		
		public final float[] getColors() {
			return this.colors;
		}
		
	}
	
}
