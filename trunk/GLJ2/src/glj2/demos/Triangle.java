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
	
	private final GL3 gl;
	
	private final FloatBuffer locations;
	
	private final FloatBuffer colors;
	
	private final VAO vao;
	
	private final VBO[] vbos;
	
	public Triangle(final GL3 gl) {
		this.position = GLJTools.newIdentity();
		this.gl = gl;
		final int vertexCount = 3;
		this.locations = Buffers.newDirectFloatBuffer(vertexCount * 3);
		this.colors = Buffers.newDirectFloatBuffer(vertexCount * 4);
		this.vao = new VAO(gl);
		this.vbos = new VBO[2];
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
			this.vao.bind()
					.addAttribute3f(this.vbos[0] = new VBO(this.gl, this.locations.position(0), GL.GL_STATIC_DRAW))
					.addAttribute4f(this.vbos[1] = new VBO(this.gl, this.colors.position(0), GL.GL_STATIC_DRAW));
		}
		
		return this;
	}
	
	@Override
	public final void render() {
		this.vao.bind();
		this.gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3);
	}
	
	@Override
	public final void destroy() {
		this.vao.destroy();
		this.vbos[1].destroy();
		this.vbos[0].destroy();
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -8907207065164819672L;
	
}