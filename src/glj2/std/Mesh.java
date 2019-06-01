package glj2.std;

import glj2.core.GLJTools;
import glj2.core.Geometry;
import glj2.core.VAO;
import glj2.core.VBO;
import multij.tools.Tools;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;
import javax.vecmath.Matrix4f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.nativewindow.awt.DirectDataBufferInt;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public final class Mesh implements Geometry {
	
	private final Matrix4f position;
	
	private final List<FloatBuffer> buffers;
	
	private final List<VBO> vbos;
	
	private final VAO vao;
	
	private final int vertexCount;
	
	private int stride;
	
	private int drawingMode;
	
	private IntBuffer texture;
	
	private BufferedImage image;
	
	public Mesh(final GL3 gl, final int vertexCount) {
		this.position = GLJTools.newIdentity();
		this.buffers = new ArrayList<>();
		this.vbos = new ArrayList<>();
		this.vao = new VAO(gl);
		this.vertexCount = vertexCount;
		this.drawingMode = GL.GL_LINE_LOOP;
		
		for (int i = 0; i < COMPONENTS.length; ++i) {
			this.buffers.add(null);
			this.vbos.add(null);
		}
		
		this.setStride(vertexCount);
	}
	
	public final int getTexture() {
		return this.texture.get(0);
	}
	
	public final BufferedImage getImage() {
		return this.image;
	}
	
	public final Mesh setTexture(final BufferedImage image) {
		this.image = image;
		final int w = image.getWidth();
		final int h = image.getHeight();
		final DataBuffer dataBuffer = this.image.getRaster().getDataBuffer();
		final IntBuffer data;
		
		if (dataBuffer instanceof DirectDataBufferInt) {
			data = ((DirectDataBufferInt) dataBuffer).getData();
		} else {
			data = IntBuffer.wrap(((DataBufferInt) dataBuffer).getData());
		}
		
		final GL3 gl = this.getVAO().getGL();
		
		if (this.texture == null) {
			this.texture = Buffers.newDirectIntBuffer(1);
		}
		
		gl.glGenTextures(1, this.texture);
		gl.glBindTexture(GL.GL_TEXTURE_2D, this.getTexture());
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, w, h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, data.position(0));
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		
		return this;
	}
	
	public final void updateImage() {
		this.updateImage(0, 0, this.getImage().getWidth(), this.getImage().getHeight());
	}
	
	public final void updateImage(final int x, final int y, final int w, final int h) {
		final GL3 gl = this.getVAO().getGL();
		final IntBuffer data = ((DirectDataBufferInt) this.image.getRaster().getDataBuffer()).getData();
		
		gl.glBindTexture(GL.GL_TEXTURE_2D, this.getTexture());
		gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, x, y, w, h, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, data.position(0)); // FIXME data position
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
	
	public final Mesh setLocations(final FloatBuffer data, final VBO vbo) {
		this.buffers.set(LOCATIONS, data);
		this.vbos.set(LOCATIONS, vbo);
		
		return this;
	}
	
	public final FloatBuffer getLocations() {
		return this.getData(LOCATIONS);
	}
	
	public final VBO getLocationsVBO() {
		return this.getVBO(LOCATIONS);
	}
	
	public final VBO getColorsVBO() {
		return this.getVBO(COLORS);
	}
	
	public final VBO getUVsVBO() {
		return this.getVBO(UVS);
	}
	
	public final VBO getVBO(final int index) {
		if (this.vbos.get(index) == null) {
			this.vbos.set(index, newVBO(this.getVAO().getGL(), this.buffers.get(index).position(0), index));
		}
		
		return this.vbos.get(index);
	}
	
	public final Mesh setColors(final FloatBuffer data, final VBO vbo) {
		this.buffers.set(COLORS, data);
		this.vbos.set(COLORS, vbo);
		
		return this;
	}
	
	public final Mesh setUVs(final FloatBuffer data, final VBO vbo) {
		this.buffers.set(UVS, data);
		this.vbos.set(UVS, vbo);
		
		return this;
	}
	
	public final FloatBuffer getColors() {
		return this.getData(COLORS);
	}
	
	public final FloatBuffer getUVs() {
		return this.getData(UVS);
	}
	
	public final FloatBuffer getData(final int index) {
		if (this.buffers.get(index) == null) {
			this.buffers.set(index, Buffers.newDirectFloatBuffer(this.getVertexCount() * COMPONENTS[index]));
		}
		
		return (FloatBuffer) this.buffers.get(index);
	}
	
	public final int getVertexCount() {
		return this.vertexCount;
	}
	
	public final Mesh addVertex(final float x, final float y, final float z,
			final float r, final float g, final float b, final float a) {
		this.getLocations().put(new float[] { x, y, z });
		this.getColors().put(new float[] { r, g, b, a });
		
		if (!this.getLocations().hasRemaining()) {
			this.setupVAO();
		}
		
		return this;
	}
	
	public final Mesh addVertex(final float x, final float y, final float z,
			final float u, final float v) {
		this.getLocations().put(new float[] { x, y, z });
		this.getUVs().put(new float[] { u, v });
		
		if (!this.getLocations().hasRemaining()) {
			this.setupVAO();
		}
		
		return this;
	}
	
	public final Mesh addVertex(final float x, final float y, final float z,
			final float r, final float g, final float b, final float a,
			final float u, final float v) {
		this.getLocations().put(new float[] { x, y, z });
		this.getColors().put(new float[] { r, g, b, a });
		this.getUVs().put(new float[] { u, v });
		
		if (!this.getLocations().hasRemaining()) {
			this.setupVAO();
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
		
		this.maybeActivateTexture(gl);
		
		for (int i = 0; i < n; i += s) {
			gl.glDrawArrays(this.getDrawingMode(), i, s);
		}
		
		vao.bind(false);
	}
	
	@Override
	public final VAO getVAO() {
		return this.vao;
	}
	
	@Override
	protected final void finalize() throws Throwable {
		try {
			if (this.texture != null) {
				this.getVAO().getGL().glDeleteTextures(1, this.texture);
			}
		} finally {
			super.finalize();
		}
	}
	
	public final Mesh setupVAO() {
		{
			final VAO vao = this.getVAO();
			
			vao.bind(true);
			
			for (int i = 0; i < this.buffers.size(); ++i) {
				if (this.buffers.get(i) != null) {
					vao.addAttribute(this.getVBO(i));
				}
			}
			
			vao.bind(false);
		}
		
		return this;
	}
	
	private final void maybeActivateTexture(final GL3 gl) {
		if (this.texture != null) {
			gl.glActiveTexture(GL.GL_TEXTURE0 + 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, this.getTexture());
		}
	}
	
	private final void updateVBO(final int vboIndex, final int components, final int start, final int end, final Buffer data) {
		if (this.getVAO().getVbos().size() <= vboIndex) {
			Tools.debugError(this, this.getVAO(), this.getVAO().getVbos().size(), vboIndex);
		}
		
		final VBO vbo = this.getVAO().getVbos().get(vboIndex);
		final int vertexStride = Float.BYTES * components;
		final GL2ES2 gl = vbo.getGL();
		
		vbo.bind();
		gl.glBufferSubData(vbo.getTarget(), start * vertexStride, (end - start) * vertexStride, data);
	}
	
	private static final long serialVersionUID = -8907207065164819672L;
	
	public static final int LOCATION_COMPONENTS = 3;
	
	public static final int COLOR_COMPONENTS = 4;
	
	public static final int UV_COMPONENTS = 2;
	
	public static final int LOCATIONS = 0;
	
	public static final int COLORS = 1;
	
	public static final int UVS = 2;
	
	private static final int[] COMPONENTS = {
			LOCATION_COMPONENTS, COLOR_COMPONENTS, UV_COMPONENTS
	};
	
	public static final VBO newVBO(final GL2ES2 gl, final Buffer data, final int type) {
		return new VBO(gl, data, GL.GL_FLOAT, COMPONENTS[type], GL.GL_STATIC_DRAW);
	}
	
	public static final Mesh newTriangle(final GL3 gl) {
		return new Mesh(gl, 3).setDrawingMode(GL.GL_TRIANGLES);
	}
	
	public static final Mesh newQuad(final GL3 gl) {
		return new Mesh(gl, 4).setDrawingMode(GL.GL_TRIANGLE_FAN);
	}
	
	public static final Mesh newQuad(final GL3 gl, final BufferedImage image) {
		return newQuad(gl)
				.setTexture(image)
				.addVertex(-0.5F, -0.5F, 0F, 0F, 0F)
				.addVertex(+0.5F, -0.5F, 0F, 1F, 0F)
				.addVertex(+0.5F, +0.5F, 0F, 1F, 1F)
				.addVertex(-0.5F, +0.5F, 0F, 0F, 1F);
	}
	
	public static final Mesh newPoints(final GL3 gl, final int n) {
		return new Mesh(gl, n).setDrawingMode(GL.GL_POINTS);
	}
	
}