package glj2.demos;

import static glj2.std.Mesh.newQuad;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Vector3f;

import glj2.core.ExtendedShaderProgram.Uniform1Int;
import glj2.core.Shaders;
import glj2.demos.Demo1.DefaultScene;
import glj2.std.UniformMPV;

import multij.swing.SwingTools;
import multij.tools.IllegalInstantiationException;

/**
 * @author codistmonk (creation 2014-10-18)
 */
public final class Demo4 {
	
	private Demo4() {
		throw new IllegalInstantiationException();
	}
	
	/**
	 * @param commandLineArguments
	 * <br>Not used
	 */
	public static final void main(final String[] commandLineArguments) {
		SwingTools.useSystemLookAndFeel();
		
		new DefaultScene() {
			
			@Override
			protected final void initialize(final GLAutoDrawable drawable) {
				super.initialize(drawable);
				
				final BufferedImage image1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				
				{
					final Graphics gr = image1.getGraphics();
					
					gr.setColor(Color.RED);
					gr.fillRect(0, 0, image1.getWidth(), image1.getHeight());
					gr.setColor(Color.CYAN);
					gr.fillRect(image1.getWidth() / 2, image1.getHeight() / 2, image1.getWidth() / 2, image1.getHeight() / 2);
					
					gr.dispose();
				}
				
				final BufferedImage image2 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				
				{
					final Graphics gr = image2.getGraphics();
					
					gr.setColor(Color.GREEN);
					gr.fillRect(0, 0, image2.getWidth(), image2.getHeight());
					gr.setColor(Color.MAGENTA);
					gr.fillRect(image2.getWidth() / 2, image2.getHeight() / 2, image2.getWidth() / 2, image2.getHeight() / 2);
					
					gr.dispose();
				}
				
				final GL3 gl = this.getGL();
				
				this.add("texture0", Shaders.newProgramV3F4(gl))
						.addUniformSetters(
								new UniformMPV(this.getProjectionView().getMatrix()),
								new Uniform1Int("vertexUV", 0))
						.addGeometries(
								this.add("quad1", newQuad(gl, image1)),
								this.add("quad2", newQuad(gl, image2)));
				
				this.getGeometry("quad1").getPosition().setTranslation(new Vector3f(-1F, -1F, 0F));
			}
			
			private static final long serialVersionUID = 1724345842755345704L;
			
		}.show();
	}
	
}
