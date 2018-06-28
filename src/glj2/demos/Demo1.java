package glj2.demos;

import static glj2.core.Shaders.*;
import static glj2.demos.Polygon.newTriangle;
import static multij.tools.Tools.debugPrint;

import glj2.core.ExtendedShaderProgram.UniformMatrix4FloatBuffer;
import glj2.core.GLSwingContext;
import glj2.core.Orbiter;
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
				
				this.add("tr 1", newTriangle(gl).setDrawingMode(GL.GL_LINE_LOOP)
						.addVertex(0F, 0F, +1F, 1F, 0F, 0F, 1F)
						.addVertex(1F, 0F, +1F, 0F, 1F, 0F, 1F)
						.addVertex(0F, 1F, +1F, 0F, 0F, 1F, 1F));
				this.add("tr 2", newTriangle(gl)
						.addVertex(0F, 0F, -1F, 0F, 1F, 1F, 1F)
						.addVertex(1F, 0F, -1F, 1F, 0F, 1F, 1F)
						.addVertex(0F, 1F, -1F, 1F, 1F, 0F, 1F));
			}
			
			/**
			 * {@value}.
			 */
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
			
			this.setGeometry();
			this.setShaders();
			this.setOrbiter();
		}
		
		protected void setGeometry() {
			// NOP
		}
		
		protected void setShaders() {
//			this.add("sp 1 1", newProgramV1F1(gl));
//			this.add("sp 1 2", newProgramV1F2(gl));
//			this.add("sp 2 3", newProgramV2F3(gl));
			this.add("sp 3 3", newProgramV3F3(this.getGL()))
			.addUniformSetters(new UniformMatrix4FloatBuffer("transform", 1, true, this.getProjectionView().getBuffer()))
			.addGeometries(this.getGeometries().values());
		}
		
		protected void setOrbiter() {
			this.getOrbiter().setDistance(8F);
			this.getOrbiter().setClippingDepth(4F);
		}
		
		@Override
		protected final void reshaped() {
			this.getCamera().setProjectionType(ProjectionType.PERSPECTIVE).setProjection(-1F, 1F, -1F, 1F);
			this.getOrbiter().updateSceneCamera();
		}
		
		@Override
		protected final void afterRender() {
			super.afterRender();
			
			if (this.frameRate.ping()) {
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
