package glj2.std;

import javax.vecmath.Matrix4f;

import glj2.core.ExtendedShaderProgram;
import glj2.core.Geometry;
import glj2.core.MatrixConverter;
import glj2.core.ExtendedShaderProgram.AbstractUniformSetter;

/**
 * @author codistmonk (creation 2018-07-01)
 */
public final class UniformMPV extends AbstractUniformSetter {
	
	private final Matrix4f projectionView;
	
	private final MatrixConverter transform;
	
	public UniformMPV(final Matrix4f projectionView) {
		super("transform");
		this.projectionView = projectionView;
		this.transform = new MatrixConverter();
	}
	
	@Override
	public final void applyTo(final ExtendedShaderProgram program, final Geometry geometry) {
		final Matrix4f transform = this.transform.getMatrix();
		
		transform.mul(this.projectionView, geometry.getPosition());
		
		program.setUniformMatrix4fv(this.getUniformName(), 1, true, this.transform.updateBuffer());
	}
	
	private static final long serialVersionUID = -652026005716813192L;
	
}