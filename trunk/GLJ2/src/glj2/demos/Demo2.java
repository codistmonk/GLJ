package glj2.demos;

import java.awt.Dimension;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;

import glj2.core.Camera;
import glj2.core.GLSwingContext;
import glj2.core.Orbiter;
import glj2.core.Scene;
import glj2.core.Shaders;
import glj2.core.Camera.ProjectionType;
import glj2.core.ExtendedShaderProgram.UniformMatrix4FloatBuffer;
import net.sourceforge.aprog.swing.SwingTools;
import net.sourceforge.aprog.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-10-18)
 */
public final class Demo2 {
	
	private Demo2() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Not used
	 */
	public static final void main(final String[] commandLineArguments) {
		SwingTools.useSystemLookAndFeel();
		
		final GLSwingContext context = new GLSwingContext();
		
		final Scene scene = new Scene() {
			
			private final Orbiter orbiter = new Orbiter(this);
			
			{
				this.orbiter.addTo(context.getCanvas());
			}
			
			@Override
			protected final void initialize(final GLAutoDrawable drawable) {
				super.initialize(drawable);
				
				final GL2ES2 gl = this.getGL();
				
				this.add("normal", Shaders.newProgramV3F3(gl))
					.addUniformSetters(new UniformMatrix4FloatBuffer("transform", 1, true, this.getProjectionView().getBuffer()))
					.addGeometries(this.add("quad", new Quad(this.getGL())
						.addVertex(0F, 0F, 0F, 1F, 0F, 0F, 1F)
						.addVertex(1F, 0F, 0F, 1F, 1F, 0F, 1F)
						.addVertex(1F, 1F, 0F, 0F, 1F, 0F, 1F)
						.addVertex(0F, 1F, 0F, 0F, 1F, 1F, 1F)));
				
				this.orbiter.setClippingDepth(4F);
				this.orbiter.setDistance(8F);
			}
			
			@Override
			protected final void reshaped() {
				final Camera camera = this.getCamera();
				
				final Dimension canvasSize = camera.getCanvasSize();
				final float aspectRatio = (float) canvasSize.width / canvasSize.height;
				
				camera.setProjectionType(ProjectionType.PERSPECTIVE).setProjection(-aspectRatio, aspectRatio, -1F, 1F);
				
				this.orbiter.updateSceneCamera();
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 1724345842755345704L;
			
		};
		
		context.getCanvas().addGLEventListener(scene);
		
		context.show();
	}
	
}
