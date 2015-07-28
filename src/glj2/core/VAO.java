package glj2.core;

import com.jogamp.common.nio.Buffers;

import java.io.Serializable;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import multij.tools.Tools;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public final class VAO implements Serializable {
	
	private final GL3 gl;
	
	private final IntBuffer vao;
	
	private boolean bound;
	
	private int attributeCount;
	
	private final List<VBO> vbos;
	
	public VAO(final GL3 gl) {
		this.gl = gl;
		this.vao = Buffers.newDirectIntBuffer(1);
		this.vbos = new ArrayList<>();
		
		gl.glGenVertexArrays(1, this.vao);
	}
	
	@SuppressWarnings("unchecked")
	public final <T extends GL3> T getGL() {
		return (T) this.gl;
	}
	
	public final List<VBO> getVbos() {
		return this.vbos;
	}
	
	public final boolean isBound() {
		return this.bound;
	}
	
	public final void checkBound() {
		if (!this.isBound()) {
			throw new IllegalStateException();
		}
	}
	
	public final VAO bind(final boolean bind) {
		this.gl.glBindVertexArray(bind ? this.vao.get(0) : 0);
		
		this.bound = bind;
		
		return this;
	}
	
	public final VAO addAttribute1f(final VBO vbo) {
		return this.addAttributeNf(vbo, 1);
	}
	
	public final VAO addAttribute2f(final VBO vbo) {
		return this.addAttributeNf(vbo, 2);
	}
	
	public final VAO addAttribute3f(final VBO vbo) {
		return this.addAttributeNf(vbo, 3);
	}
	
	public final VAO addAttribute4f(final VBO vbo) {
		return this.addAttributeNf(vbo, 4);
	}
	
	public final VAO addAttributeNf(final VBO vbo, final int n) {
		return this.addAttribute(vbo, n, GL.GL_FLOAT, false, 0, 0L);
	}
	
	public final VAO addIndices(final VBO vbo) {
		this.checkBound();
		
		if (vbo.getTarget() != GL.GL_ELEMENT_ARRAY_BUFFER) {
			Tools.debugError("Warning: Unexpected target:", vbo.getTarget(), "(expected:", GL.GL_ELEMENT_ARRAY_BUFFER + ")");
		}
		
		vbo.bind();
		
		this.getVbos().add(vbo);
		
		return this;
	}
	
	public final VAO addAttribute(final VBO vbo,
			final int attributeComponentCount, final int attributeComponentType,
			final boolean normalizeFixedPoint, final int stride, final long pointerBufferOffset) {
		this.checkBound();
		
		if (vbo.getTarget() != GL.GL_ARRAY_BUFFER) {
			Tools.debugError("Warning: Unexpected target:", vbo.getTarget(), "(expected:", GL.GL_ARRAY_BUFFER + ")");
		}
		
		vbo.bind();
		this.gl.glEnableVertexAttribArray(this.attributeCount);
		this.gl.glVertexAttribPointer(this.attributeCount,
				attributeComponentCount, attributeComponentType, normalizeFixedPoint, stride, pointerBufferOffset);
		
		++this.attributeCount;
		
		this.getVbos().add(vbo);
		
		return this;
	}
	
	@Override
	protected final void finalize() throws Throwable {
		try {
			this.gl.glDeleteVertexArrays(1, this.vao);
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = 7694498137573039997L;
	
}