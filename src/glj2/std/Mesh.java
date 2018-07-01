package glj2.std;

import glj2.core.GLJTools;
import glj2.core.Geometry;
import glj2.core.VAO;
import glj2.core.VBO;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.vecmath.Matrix4f;

import com.jogamp.common.nio.Buffers;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public final class Mesh implements Geometry {
	
	private final Matrix4f position;
	
	private final List<Buffer> buffers;
	
	private final VAO vao;
	
	private final int vertexCount;
	
	private int drawingMode;
	
	public Mesh(final GL3 gl, final int vertexCount) {
		this.position = GLJTools.newIdentity();
		this.buffers = new ArrayList<>();
		this.vao = new VAO(gl);
		this.vertexCount = vertexCount;
		this.drawingMode = GL.GL_LINE_LOOP;
		
		this.buffers.add(Buffers.newDirectFloatBuffer(vertexCount * LOCATION_COMPONENTS));
		
		this.setStride(vertexCount);
	}
	
	public final int getDrawingMode() {
		return this.drawingMode;
	}
	
	public final Mesh setDrawingMode(final int drawingMode) {
		this.drawingMode = drawingMode;
		
		return this;
	}
	
	@Override
	public final Matrix4f getPosition() {
		return this.position;
	}
	
	public final FloatBuffer getLocations() {
		return (FloatBuffer) this.buffers.get(LOCATIONS);
	}
	
	public final FloatBuffer getColors() {
		if (this.buffers.size() <= 1) {
			this.buffers.add(Buffers.newDirectFloatBuffer(this.getVertexCount() * COLOR_COMPONENTS));
		}
		
		return (FloatBuffer) this.buffers.get(COLORS);
	}
	
	public final FloatBuffer getUVs() {
		if (this.buffers.size() <= 1) {
			this.buffers.add(Buffers.newDirectFloatBuffer(this.getVertexCount() * UV_COMPONENTS));
		}
		
		return (FloatBuffer) this.buffers.get(UVS);
	}
	
	public final int getVertexCount() {
		return this.vertexCount;
	}
	
	public final Mesh addVertex(final float x, final float y, final float z,
			final float r, final float g, final float b, final float a) {
		this.getLocations().put(new float[] { x, y, z });
		this.getColors().put(new float[] { r, g, b, a });
		
		if (!this.getLocations().hasRemaining()) {
			final GL3 gl = this.getVAO().getGL();
			
			this.getVAO().bind(true)
					.addAttribute3f(new VBO(gl, this.getLocations().position(0), GL.GL_STATIC_DRAW))
					.addAttribute4f(new VBO(gl, this.getColors().position(0), GL.GL_STATIC_DRAW))
					.bind(false);
		}
		
		return this;
	}
	
	public final Mesh addVertex(final float x, final float y, final float z,
			final float u, final float v) {
		this.getLocations().put(new float[] { x, y, z });
		this.getColors().put(new float[] { u, v });
		
		if (!this.getLocations().hasRemaining()) {
			final GL3 gl = this.getVAO().getGL();
			
			this.getVAO().bind(true)
			.addAttribute3f(new VBO(gl, this.getLocations().position(0), GL.GL_STATIC_DRAW))
			.addAttribute2f(new VBO(gl, this.getUVs().position(0), GL.GL_STATIC_DRAW))
			.bind(false);
		}
		
		return this;
	}
	
	public final void updateLocations() {
		this.updateLocations(0, this.getVertexCount());
	}
	
	public final void updateLocations(final int start, final int end) {
		this.updateLocations(start, end, this.getLocations().position(start * LOCATION_COMPONENTS));
	}
	
	public final void updateLocations(final int start, final int end, final Buffer data) {
		this.updateVBO(LOCATIONS, LOCATION_COMPONENTS, start, end, data);
	}
	
	public final void updateColors() {
		this.updateColors(0, this.getVertexCount());
	}
	
	public final void updateColors(final int start, final int end) {
		this.updateColors(start, end, this.getColors().position(start * COLOR_COMPONENTS));
	}
	
	public final void updateColors(final int start, final int end, final Buffer data) {
		this.updateVBO(COLORS, COLOR_COMPONENTS, start, end, data);
	}
	
	public final void updateUVs() {
		this.updateColors(0, this.getVertexCount());
	}
	
	public final void updateUVs(final int start, final int end) {
		this.updateColors(start, end, this.getColors().position(start * UV_COMPONENTS));
	}
	
	public final void updateUVs(final int start, final int end, final Buffer data) {
		this.updateVBO(UVS, UV_COMPONENTS, start, end, data);
	}
	
	private int stride;
	
	public final int getStride() {
		return this.stride;
	}
	
	public final Mesh setStride(final int stride) {
		this.stride = stride;
		
		return this;
	}
	
	@Override
	public final void render() {
		final VAO vao = this.getVAO();
		final GL3 gl = vao.getGL();
		final int n = this.getVertexCount();
		final int s = this.getStride();
		
		vao.bind(true);
		for (int i = 0; i < n; i += s) {
			gl.glDrawArrays(this.getDrawingMode(), i, s);
		}
		vao.bind(false);
	}
	
	@Override
	public final VAO getVAO() {
		return this.vao;
	}
	
	private final void updateVBO(final int vboIndex, final int components, final int start, final int end, final Buffer data) {
		final VBO vbo = this.getVAO().getVbos().get(vboIndex);
		final int vertexStride = Float.BYTES * components;
		
		vbo.bind();
		vbo.getGL().glBufferSubData(vbo.getTarget(), start * vertexStride, (end - start) * vertexStride, data);
	}
	
	private static final long serialVersionUID = -8907207065164819672L;
	
	public static final int LOCATION_COMPONENTS = 3;
	
	public static final int COLOR_COMPONENTS = 4;
	
	public static final int UV_COMPONENTS = 2;
	
	public static final int LOCATIONS = 0;
	
	public static final int COLORS = 1;
	
	public static final int UVS = 1;
	
	public static final Mesh newTriangle(final GL3 gl) {
		return new Mesh(gl, 3).setDrawingMode(GL.GL_TRIANGLES);
	}
	
	public static final Mesh newQuad(final GL3 gl) {
		return new Mesh(gl, 4).setDrawingMode(GL.GL_TRIANGLE_FAN);
	}
	
	public static final Mesh newPoints(final GL3 gl, final int n) {
		return new Mesh(gl, n).setDrawingMode(GL.GL_POINTS);
	}
	
}