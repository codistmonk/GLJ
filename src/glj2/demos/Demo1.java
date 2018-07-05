package glj2.demos;

import static glj2.core.Shaders.*;
import static glj2.std.Mesh.newTriangle;
import static multij.tools.Tools.debugPrint;

import glj2.std.FrameRate;
import glj2.std.Orbiter;
import glj2.std.UniformMPV;
import glj2.core.GLJTools;
import glj2.core.GLSwingContext;
import glj2.core.Scene;
import glj2.core.Camera.ProjectionType;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;

import multij.swing.SwingTools;
import multij.tools.IllegalInstantiationException;

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
		
		new DefaultScene() {
			
			@Override
			protected final void setGeometry() {
				final GL3 gl = this.getGL();
				
				this.getShaderProgram("sp 3 3").addGeometries(
						newTriangle(gl).setDrawingMode(GL.GL_LINE_LOOP)
						.addVertex(0F, 0F, +1F, 1F, 0F, 0F, 1F)
						.addVertex(1F, 0F, +1F, 0F, 1F, 0F, 1F)
						.addVertex(0F, 1F, +1F, 0F, 0F, 1F, 1F),
						newTriangle(gl)
						.addVertex(0F, 0F, -1F, 0F, 1F, 1F, 1F)
						.addVertex(1F, 0F, -1F, 1F, 0F, 1F, 1F)
						.addVertex(0F, 1F, -1F, 1F, 1F, 0F, 1F));
			}
			
			private static final long serialVersionUID = 2457973938713347874L;
			
		}.show();
	}
	
	/**
	 * @author codistmonk (creation 2017-08-09)
	 */
	public static abstract class DefaultScene extends Scene {
		
		private final GLSwingContext context = new GLSwingContext();
		
		private final FrameRate frameRate = new FrameRate(1_000_000_000L);
		
		private final Orbiter orbiter = new Orbiter(this).addTo(this.getContext().getCanvas());
		
		public final GLSwingContext getContext() {
			return this.context;
		}
		
		public final FrameRate getFrameRate() {
			return this.frameRate;
		}
		
		public final Orbiter getOrbiter() {
			return this.orbiter;
		}
		
		@Override
		protected void initialize(final GLAutoDrawable drawable) {
			final GL3 gl = this.getGL();
			final String renderer = gl.glGetString(GL.GL_RENDERER);
			final String version = gl.glGetString(GL.GL_VERSION);
			
			debugPrint("GL:", gl);
			debugPrint("Renderer:", renderer);
			debugPrint("Version:", version);
			
			this.setShaders();
			this.setGeometry();
			this.initializeOrbiter();
		}
		
		protected void setShaders() {
			this.add("sp 3 3", newProgramV3F3(this.getGL()))
			.addUniformSetters(new UniformMPV(this.getProjectionView().getMatrix()));
		}
		
		protected void setGeometry() {
			// NOP
		}
		
		protected void initializeOrbiter() {
			this.getOrbiter().setDistance(8F);
			this.getOrbiter().setClippingDepth(4F);
		}
		
		@Override
		protected final void reshaped() {
			GLJTools.setProjectionWithCanvasAspectRatio(this.getCamera(), ProjectionType.PERSPECTIVE);
			
			this.getOrbiter().updateSceneCamera();
		}
		
		@Override
		protected final void afterRender() {
			super.afterRender();
			
			if (this.getFrameRate().ping()) {
				this.getContext().getFrame().setTitle("" + this.frameRate.get());
			}
		}
		
		public final void show() {
			this.getContext().getCanvas().addGLEventListener(this);
			this.getContext().show();
		}
		
		private static final long serialVersionUID = 6112962024541718530L;
		
	}
	
}
