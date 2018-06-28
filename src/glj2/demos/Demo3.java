package glj2.demos;

import static glj2.core.Shaders.*;
import static glj2.demos.Polygon.newPoints;
import static multij.tools.Tools.debugPrint;

import glj2.core.ExtendedShaderProgram.UniformMatrix4FloatBuffer;
import glj2.core.GLSwingContext;
import glj2.core.Orbiter;
import glj2.core.Scene;
import glj2.core.Camera.ProjectionType;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;

import multij.swing.SwingTools;
import multij.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2016-12-20)
 */
public final class Demo3 {
	
	private Demo3() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Unused
	 */
	public static final void main(final String[] commandLineArguments) {
		SwingTools.useSystemLookAndFeel();
		
		final GLSwingContext context = new GLSwingContext();
		
		final Scene scene = new Scene() {
			
			private final FrameRate frameRate = new FrameRate(1_000_000_000L);
			
			private final Orbiter orbiter = new Orbiter(this);
			
			{
				this.orbiter.addTo(context.getCanvas());
			}
			
			@Override
			protected final void initialize(final GLAutoDrawable drawable) {
				final GL4 gl = this.getGL();
				final String renderer = gl.glGetString(GL.GL_RENDERER);
				final String version = gl.glGetString(GL.GL_VERSION);
				
				debugPrint("GL:", gl);
				debugPrint("Renderer:", renderer);
				debugPrint("Version:", version);
				
				{
					final int n = 800;
					final int vertexCount = cube(n + 1) - cube(n - 1);
					
					debugPrint("vertexCount:", vertexCount);
					
					final Polygon cloud = newPoints(gl, vertexCount);
					
					for (int x = 0; x <= n; ++x) {
						for (int y = 0; y <= n; ++y) {
							for (int z = 0; z <= n; ++z) {
								if (x % n == 0 || y % n == 0 || z % n == 0) {
									cloud.addVertex(x / (float) n - 0.5F, y / (float) n - 0.5F, z / (float) n - 0.5F,
											x / (float) n, y / (float) n, z / (float) n, 1F);
								}
							}
						}
					}
					
					this.add("cloud", cloud);
				}
				
				this.add("sp 3 3", newProgramV3F3(gl))
					.addUniformSetters(new UniformMatrix4FloatBuffer("transform", 1, true, this.getProjectionView().getBuffer()))
					.addGeometries(this.getGeometries().values());
				
				this.orbiter.setDistance(8F);
				this.orbiter.setClippingDepth(4F);
			}
			
			@Override
			protected final void reshaped() {
				this.getCamera().setProjectionType(ProjectionType.PERSPECTIVE).setProjection(-1F, 1F, -1F, 1F);
				this.orbiter.updateSceneCamera();
			}
			
			@Override
			protected final void afterRender() {
				super.afterRender();
				
				if (this.frameRate.ping()) {
					context.getFrame().setTitle("" + this.frameRate.get());
				}
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 2457973938713347874L;
			
		};
		
		context.getCanvas().addGLEventListener(scene);
		context.show();
	}
	
	public static final int cube(final int x) {
		return x * x * x;
	}
	
}
