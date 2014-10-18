package glj2.demos;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;

import glj2.core.GLSwingContext;
import glj2.core.MatrixConverter;
import glj2.core.Orbiter;
import glj2.core.Scene;
import glj2.core.Shaders;
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
			
			private final MatrixConverter transform = new MatrixConverter();
			
			@Override
			protected final void initialize(final GLAutoDrawable drawable) {
				super.initialize(drawable);
				
				final GL2ES2 gl = this.getGL();
				
				this.add(
						this.add("sp", Shaders.newProgramV3F3(gl)),
						this.add("quad", new Quad(this.getGL())
						.addVertex(0F, 0F, 0F, 1F, 0F, 0F, 1F)
						.addVertex(1F, 0F, 0F, 1F, 1F, 0F, 1F)
						.addVertex(1F, 1F, 0F, 0F, 1F, 0F, 1F)
						.addVertex(0F, 1F, 0F, 0F, 1F, 1F, 1F)));
			}
			
			@Override
			protected final void reshaped() {
				this.getCamera().setPerspective(-1F, 1F, -1F, 1F, 0.5F, 40F);
			}
			
			@Override
			protected final void beforeRender() {
				super.beforeRender();
				
				this.getCamera().getProjectionView(this.transform.getMatrix());
				
				this.getShaderProgram("sp").useProgram(true).setUniformMatrix4fv(
						"transform", 1, true, this.transform.updateBuffer());
			}
			
			/**
			 * {@value}.
			 */
			private static final long serialVersionUID = 1724345842755345704L;
			
		};
		
		context.getCanvas().addGLEventListener(scene);
		new Orbiter(scene).addTo(context.getCanvas());
		
		context.show();
	}
	
}
