package glj2.demos;

import java.awt.Dimension;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import glj2.core.Camera;
import glj2.core.ExtendedShaderProgram;
import glj2.core.ExtendedShaderProgram.UniformSetter;
import glj2.core.GLSwingContext;
import glj2.core.Geometry;
import glj2.core.MatrixConverter;
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
					.addGeometries(this.add("quad1", new Quad(this.getGL())
						.addVertex(0F, 0F, 0F, 1F, 0F, 0F, 1F)
						.addVertex(1F, 0F, 0F, 1F, 1F, 0F, 1F)
						.addVertex(1F, 1F, 0F, 0F, 1F, 0F, 1F)
						.addVertex(0F, 1F, 0F, 0F, 1F, 1F, 1F)));
				
				this.add("billboarded", Shaders.newProgramV3F3(gl))
						.addUniformSetters(new UniformSetter("transform") {
							
							private final MatrixConverter billboardingTransform = new MatrixConverter();
							
							@Override
							public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
								final Matrix4f billboardingMatrix = this.billboardingTransform.getMatrix();
								final Camera camera = getCamera();
								
								billboardingMatrix.mul(camera.getView(), geometry.getPosition());
								billboardingMatrix.mul(camera.getProjection(), resetRotation(billboardingMatrix));
								
								program.setUniformMatrix4fv(this.getUniformName(), 1, true, this.billboardingTransform.updateBuffer());
							}
							
							/**
							 * {@value}.
							 */
							private static final long serialVersionUID = 5152623125713666882L;
							
						})
						.addGeometries(this.add("quad2", new Quad(this.getGL())
						.addVertex(0F, 0F, 0F, 1F, 0F, 0F, 1F)
						.addVertex(1F, 0F, 0F, 1F, 1F, 0F, 1F)
						.addVertex(1F, 1F, 0F, 0F, 1F, 0F, 1F)
						.addVertex(0F, 1F, 0F, 0F, 1F, 1F, 1F)));
				
				this.getGeometry("quad2").getPosition().setTranslation(new Vector3f(-1F, 0F, 0F));
				
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
	
	public static final Matrix4f resetRotation(final Matrix4f matrix) {
		matrix.m00 = 1F;
		matrix.m10 = 0F;
		matrix.m20 = 0F;
		matrix.m01 = 0F;
		matrix.m11 = 1F;
		matrix.m21 = 0F;
		matrix.m02 = 0F;
		matrix.m12 = 0F;
		matrix.m22 = 1F;
		
		return matrix;
	}
	
}
