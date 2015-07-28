package glj2.core;

import static multij.tools.Tools.unchecked;

import com.jogamp.common.nio.Buffers;

import java.io.Serializable;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;

/**
 * @author codistmonk (creation 2014-08-17)
 */
public final class VBO implements Serializable {
	
	private final GL2ES2 gl;
	
	private final IntBuffer vbo;
	
	private final int target;
	
	public VBO(final GL2ES2 gl, final Buffer data, final int usage) {
		this(gl, GL.GL_ARRAY_BUFFER, data, usage);
	}
	
	public VBO(final GL2ES2 gl, final int target, final Buffer data, final int usage) {
		this.gl = gl;
		this.vbo = Buffers.newDirectIntBuffer(1);
		this.target = target;
		
		gl.glGenBuffers(1, this.vbo);
		this.bind();
		gl.glBufferData(target, data.capacity() * getDatumSize(data), data, usage);
	}
	
	@SuppressWarnings("unchecked")
	public final <T extends GL2ES2> T getGL() {
		return (T) this.gl;
	}
	
	public final int getTarget() {
		return this.target;
	}
	
	public final VBO bind() {
		this.gl.glBindBuffer(this.getTarget(), this.vbo.get(0));
		
		return this;
	}
	
	@Override
	protected final void finalize() throws Throwable {
		try {
			this.gl.glDeleteBuffers(1, this.vbo);
		} finally {
			super.finalize();
		}
	}
	
	public static final int getDatumSize(final Buffer buffer) {
		final String bufferClassName = buffer.getClass().getName();
		final Matcher matcher = PATTERN.matcher(bufferClassName);
		
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid buffer type: " + bufferClassName);
		}
		
		String datumClassName = matcher.group(2);
		
		if ("Int".equals(datumClassName)) {
			datumClassName = "Integer";
		}
		
		try {
			return Class.forName("java.lang." + datumClassName).getField("BYTES").getInt(null);
		} catch (final Exception exception) {
			throw unchecked(exception);
		}
	}
	
	/**
	 * {@value}.
	 */
	private static final long serialVersionUID = -4781678108773022509L;
	
	private static final Pattern PATTERN = Pattern.compile("(.*Direct)?(.+)Buffer.*");
	
}