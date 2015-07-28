package glj2.demos;

import glj2.core.GLJTools;
import glj2.core.Geometry;
import glj2.core.VAO;
import glj2.core.VBO;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.vecmath.Matrix4f;

import com.jogamp.common.nio.Buffers;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public final class Triangle implements Geometry {
	
	private final Matrix4f position;
	
	private final FloatBuffer locations;
	
	private final FloatBuffer colors;
	
	private final VAO vao;
	
	public Triangle(final GL3 gl) {
		this.position = GLJTools.newIdentity();
		final int vertexCount = 3;
		this.locations = Buffers.newDirectFloatBuffer(vertexCount * 3);
		this.colors = Buffers.newDirectFloatBuffer(vertexCount * 4);
		this.vao = new VAO(gl);
	}
	
	@Override
	public final Matrix4f getPosition() {
		return this.position;
	}
	
	public final Triangle addVertex(final float x, final float y, final float z,
			final float r, final float g, final float b, final float a) {
		this.locations.put(new float[] { x, y, z });
		this.colors.put(new float[] { r, g, b, a });
		
		if (!this.locations.hasRemaining()) {
			final GL3 gl = this.getVAO().getGL();
			
			this.getVAO().bind(true)
					.addAttribute3f(new VBO(gl, this.locations.position(0), GL.GL_STATIC_DRAW))
					.addAttribute4f(new VBO(gl, this.colors.position(0), GL.GL_STATIC_DRAW))
					.bind(false);
		}
		
		return this;
	}
	
	@Override
	public final void render() {
		final VAO vao = this.getVAO();
		
		vao.bind(true);
		vao.getGL().glDrawArrays(GL.GL_TRIANGLES, 0, 3);
		vao.bind(false);
	}
	
	@Override
	public final VAO getVAO() {
		return this.vao;
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8907207065164819672L;
	
}