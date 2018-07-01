package glj2.demos;

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
public final class Polygon implements Geometry {
	
	private final Matrix4f position;
	
	private final List<Buffer> buffers;
	
	private final VAO vao;
	
	private final int vertexCount;
	
	private int drawingMode;
	
	public Polygon(final GL3 gl, final int vertexCount) {
		this.position = GLJTools.newIdentity();
		this.buffers = new ArrayList<>();
		this.vao = new VAO(gl);
		this.vertexCount = vertexCount;
		this.drawingMode = GL.GL_LINE_LOOP;
		
		this.buffers.add(Buffers.newDirectFloatBuffer(vertexCount * LOCATION_COMPONENTS));
		this.buffers.add(Buffers.newDirectFloatBuffer(vertexCount * COLOR_COMPONENTS));
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
	
	public final FloatBuffer getLocations() {
		return (FloatBuffer) this.buffers.get(LOCATIONS);
	}
	
	public final FloatBuffer getColors() {
		return (FloatBuffer) this.buffers.get(COLORS);
	}
	
	public final int getVertexCount() {
		return this.vertexCount;
	}
	
	public final Polygon addVertex(final float x, final float y, final float z,
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
	
	public final void updateLocations() {
		this.updateLocations(0, this.getVertexCount() * LOCATION_COMPONENTS);
	}
	
	public final void updateLocations(final int start, final int end) {
		this.updateLocations(start, end, this.getLocations().position(start * LOCATION_COMPONENTS));
	}
	
	public final void updateLocations(final int start, final int end, final Buffer data) {
		this.updateVBO(LOCATIONS, LOCATION_COMPONENTS, start, end, data);
	}
	
	public final void updateColors() {
		this.updateColors(0, this.getVertexCount() * COLOR_COMPONENTS);
	}
	
	public final void updateColors(final int start, final int end) {
		this.updateColors(start, end, this.getColors().position(start * COLOR_COMPONENTS));
	}
	
	public final void updateColors(final int start, final int end, final Buffer data) {
		this.updateVBO(COLORS, COLOR_COMPONENTS, start, end, data);
	}
	
	@Override
	public final void render() {
		final VAO vao = this.getVAO();
		
		vao.bind(true);
		vao.getGL().glDrawArrays(this.getDrawingMode(), 0, this.getVertexCount());
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
	
	public static final int LOCATIONS = 0;
	
	public static final int COLORS = 1;
	
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