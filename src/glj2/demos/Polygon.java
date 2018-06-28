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
public final class Polygon implements Geometry {
	
	private final Matrix4f position;
	
	private final FloatBuffer locations;
	
	private final FloatBuffer colors;
	
	private final VAO vao;
	
	private int drawingMode;
	
	private int vertexCount;
	
	public Polygon(final GL3 gl, final int vertexCount) {
		this.position = GLJTools.newIdentity();
		this.locations = Buffers.newDirectFloatBuffer(vertexCount * 3);
		this.colors = Buffers.newDirectFloatBuffer(vertexCount * 4);
		this.vao = new VAO(gl);
		this.vertexCount = vertexCount;
		this.drawingMode = GL.GL_LINE_LOOP;
	}
	
	public final int getDrawingMode() {
		return this.drawingMode;
	}
	
	public final Polygon setDrawingMode(final int drawingMode) {
		this.drawingMode = drawingMode;
		
		return this;
	}
	
	@Override
	public final Matrix4f getPosition() {
		return this.position;
	}
	
	public final Polygon addVertex(final float x, final float y, final float z,
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
		vao.getGL().glDrawArrays(this.getDrawingMode(), 0, this.vertexCount);
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
	
	public static final Polygon newTriangle(final GL3 gl) {
		return new Polygon(gl, 3).setDrawingMode(GL.GL_TRIANGLES);
	}
	
	public static final Polygon newQuad(final GL3 gl) {
		return new Polygon(gl, 4).setDrawingMode(GL.GL_TRIANGLE_FAN);
	}
	
	public static final Polygon newPoints(final GL3 gl, final int n) {
		return new Polygon(gl, n).setDrawingMode(GL.GL_POINTS);
	}
	
}