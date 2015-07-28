package glj2.core;

import java.io.Serializable;
import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;

import com.jogamp.common.nio.Buffers;

/**
 * @author codistmonk (creation 2014-08-18)
 */
public final class MatrixConverter implements Serializable {
	
	private final Matrix4f matrix = new Matrix4f();
	
	private final FloatBuffer buffer = Buffers.newDirectFloatBuffer(16);
	
	public final Matrix4f getMatrix() {
		return this.matrix;
	}
	
	public final FloatBuffer getBuffer() {
		return this.buffer;
	}
	
	public final FloatBuffer updateBuffer() {
		this.getBuffer().position(0);
		
		this.getBuffer().put(this.getMatrix().m00);
		this.getBuffer().put(this.getMatrix().m01);
		this.getBuffer().put(this.getMatrix().m02);
		this.getBuffer().put(this.getMatrix().m03);
		this.getBuffer().put(this.getMatrix().m10);
		this.getBuffer().put(this.getMatrix().m11);
		this.getBuffer().put(this.getMatrix().m12);
		this.getBuffer().put(this.getMatrix().m13);
		this.getBuffer().put(this.getMatrix().m20);
		this.getBuffer().put(this.getMatrix().m21);
		this.getBuffer().put(this.getMatrix().m22);
		this.getBuffer().put(this.getMatrix().m23);
		this.getBuffer().put(this.getMatrix().m30);
		this.getBuffer().put(this.getMatrix().m31);
		this.getBuffer().put(this.getMatrix().m32);
		this.getBuffer().put(this.getMatrix().m33);
		
		return (FloatBuffer) this.getBuffer().position(0);
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -3809492789857545268L;
	
}