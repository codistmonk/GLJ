package glj2.demos;

import static glj2.core.Shaders.*;
import static net.sourceforge.aprog.tools.Tools.debugPrint;
import glj2.core.GLSwingContext;
import glj2.core.MatrixConverter;
import glj2.core.Orbiter;
import glj2.core.Scene;
import glj2.core.Camera.ProjectionType;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;

import net.sourceforge.aprog.swing.SwingTools;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-08-16)
 */
public final class Demo1 {
	
	private Demo1() {
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
			
			private final MatrixConverter transform = new MatrixConverter();
			
			@Override
			protected final void initialize(final GLAutoDrawable drawable) {
				final GL4 gl = this.getGL();
				final String renderer = gl.glGetString(GL.GL_RENDERER);
				final String version = gl.glGetString(GL.GL_VERSION);
				
				debugPrint("GL:", gl);
				debugPrint("Renderer:", renderer);
				debugPrint("Version:", version);
				
				{
					gl.glEnable(GL.GL_DEPTH_TEST);
					gl.glDepthFunc(GL.GL_LESS);
				}
				
				this.add("tr 1", new Triangle(gl)
						.addVertex(0F, 0F, -2F, 1F, 0F, 0F, 1F)
						.addVertex(1F, 0F, -2F, 0F, 1F, 0F, 1F)
						.addVertex(0F, 1F, -2F, 0F, 0F, 1F, 1F));
				this.add("tr 2", new Triangle(gl)
						.addVertex(0F, 0F, -3F, 0F, 1F, 1F, 1F)
						.addVertex(1F, 0F, -3F, 1F, 0F, 1F, 1F)
						.addVertex(0F, 1F, -3F, 1F, 1F, 0F, 1F));
				
				this.add("sp 1 1", newProgramV1F1(gl));
				this.add("sp 1 2", newProgramV1F2(gl));
				this.add("sp 2 3", newProgramV2F3(gl));
				this.add("sp 3 3", newProgramV3F3(gl));
				
				this.add(this.getShaderProgram("sp 3 3"), this.getGeometries().values());
			}
			
			@Override
			protected final void reshaped() {
				this.getCamera().setProjectionType(ProjectionType.PERSPECTIVE).setProjection(-1F, 1F, -1F, 1F, 0.5F, 40F);
			}
			
			@Override
			protected final void beforeRender() {
				super.beforeRender();
				
				this.getCamera().getProjectionView(this.transform.getMatrix());
				
				this.getShaderProgram("sp 3 3").useProgram(true).setUniformMatrix4fv(
						"transform", 1, true, this.transform.updateBuffer());
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
		new Orbiter(scene).addTo(context.getCanvas());
		context.show();
	}
	
}
